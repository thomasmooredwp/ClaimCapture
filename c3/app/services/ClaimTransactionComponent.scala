package services

import gov.dwp.exceptions.DwpRuntimeException
import play.api.db.DB
import play.api.Play.current
import anorm._
import play.api.i18n.Lang
import anorm.SqlParser._
import anorm.~
import play.api.Logger
import app.ConfigProperties._

trait ClaimTransactionComponent {
  val claimTransaction: ClaimTransaction

  class ClaimTransaction {

    def generateId: String = {
      DB.withConnection("carers") {
        connection =>
          try {
            val statement = connection.prepareCall("select get_new_transaction_id(?);")
            statement.setString(1, getStringProperty("origin.tag"))
            statement.execute()
            val result = statement getResultSet()
            result.next
            result.getString("get_new_transaction_id")
          }
          catch {
            case e: java.lang.Exception => throw new UnavailableTransactionIdException("Cannot generate an unique transaction ID.", e)
          }
      }
    }

    /**
     * Record that an ID has been used
     */
    def registerId(id: String, statusCode: String, claimType: Int, jsEnabled: Int, originTag: String): Unit = DB.withConnection("carers") {
      implicit c =>
        SQL(
          """
          INSERT INTO transactionstatus (transaction_id, status, type, js_enabled, origintag)
          VALUES ({transactionId},{status},{type},{js_enabled},{originTag});
          """
        ).on("transactionId" -> id, "status" -> statusCode, "type" -> claimType, "js_enabled" -> jsEnabled, "originTag" -> originTag).execute()
    }

    /**
     * Update MI data
     */
    def recordMi(id: String, thirdParty: Boolean = false, circsChange: Option[Int] = None, lang: Option[Lang]): Unit = DB.withConnection("carers") {
      implicit c =>
        SQL(
          """
            UPDATE transactionstatus set thirdparty={thirdParty}, circs_type={circsChange}, lang={lang}
            WHERE transaction_id={transactionId};
          """
        ).on("transactionId" -> id, "thirdParty" -> (if (thirdParty) 1 else 0), "circsChange" -> circsChange, "lang" -> lang.getOrElse(Lang("en")).code).execute()
    }

    def updateStatus(id: String, statusCode: String, claimType: Int): Unit = DB.withConnection("carers") {
      implicit connection =>

        SQL(
          """
          UPDATE transactionstatus set status={status}, type={type}
          WHERE transaction_id={transactionId};
          """
        ).on("status" -> statusCode, "type" -> claimType, "transactionId" -> id).executeUpdate()
    }

    def updateEmailStatus(id: String, statusCode: Int): Unit = DB.withConnection("carers") {
      implicit connection =>

        SQL(
          """
          UPDATE transactionstatus set email={status}
          WHERE transaction_id={transactionId};
          """
        ).on("status" -> statusCode, "transactionId" -> id).executeUpdate()
    }

    def updateSaveForLaterEmailStatus(id: String, statusCode: Int): Unit = DB.withConnection("carers") {
      implicit connection =>

        SQL(
          """
          UPDATE transactionstatus set saveforlateremail={status}
          WHERE transaction_id={transactionId};
          """
        ).on("status" -> statusCode, "transactionId" -> id).executeUpdate()
    }

    val transactionStatusParser = {
      get[String]("transaction_id") ~
        get[String]("status") ~
        get[Int]("type") ~
        get[Option[Int]]("thirdparty") ~
        get[Option[Int]]("circs_type") ~
        get[Option[String]]("lang") map {
        case id ~ status ~ typeI ~ thirdparty ~ circsType ~ lang => TransactionStatus(id, status, typeI, thirdparty, circsType, lang)
      }
    }

    def getTransactionStatusById(id: String): Option[TransactionStatus] = {
      import scala.language.postfixOps
      DB.withConnection("carers") {
        implicit c =>
          SQL(
            """
          SELECT transaction_id, status,type,thirdparty,circs_type,lang
          FROM transactionstatus
          WHERE transaction_id = {id}
            """
          ).on("id" -> id)
            .as(transactionStatusParser singleOpt)
      }
    }

    /**
     * Health check
     */
    def health(): Unit = DB.withConnection("carers") {
      implicit c =>
        SQL(
          """
          SELECT 1;
          """
        ).execute()
    }

  }


  class StubClaimTransaction extends ClaimTransaction {
    override def generateId: String = "15101000001"

    override def registerId(id: String, statusCode: String, claimType: Int, jsEnabled: Int, originTag: String) {}

    override def recordMi(id: String, thirdParty: Boolean = false, circsChange: Option[Int], lang: Option[Lang]) {}

    override def updateStatus(id: String, statusCode: String, claimType: Int) {}

    override def health(): Unit = {
      Logger.warn("Stub db health check - checking for db")
      DB.withConnection("carers") {
        implicit c =>
          SQL(
            """
          SELECT 1;
            """
          ).execute()
      }
    }

    override def updateEmailStatus(id: String, statusCode: Int): Unit = {}
    override def updateSaveForLaterEmailStatus(id: String, statusCode: Int): Unit = {}
  }

}

case class TransactionStatus(transactionID: String, status: String, typeI: Int, thirdParty: Option[Int], circsChange: Option[Int], lang: Option[String])

/**
 * Exception thrown by UniqueTransactionId if it could not generate an id. The cause is described by the nested exception.
 * @param message  the detail message
 * @param nestedException  the cause
 */
class UnavailableTransactionIdException(message: String, nestedException: Exception)
  extends DwpRuntimeException(message, nestedException) {}
