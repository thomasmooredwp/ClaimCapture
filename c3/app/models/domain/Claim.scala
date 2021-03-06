package models.domain

import models.DayMonthYear
import models.view.Navigation
import play.api.i18n.Lang
import utils.ClaimEncryption

import scala.language.postfixOps
import scala.reflect.ClassTag

/**
 * Represents a Claim or Change of circumstances. Data is decomposed into sections that contain questions.
 * Each claim is identified uniquely by its statistically unique uuid. Transaction ID are also used to identify uniquely a claim, but are
 * generated only when submitting a claim. The transaction Id is then used to track the claim in all the other services and apps composing CAOL
 * and is shown in the printable form of the claim (see rendering service and casa).
 */
case class Claim(key: String, sections: List[Section] = List(), created: Long = System.currentTimeMillis(), lang: Option[Lang] = None,
                 gacid:String="", uuid: String = "", transactionId: Option[String] = None,
                 checkYAnswers: CheckYAnswers = CheckYAnswers(), saveForLaterCurrentPageData: Map[String, String] = Map())(implicit val navigation: Navigation = Navigation()) {
  def section(sectionIdentifier: Identifier): Section = {
    sections.find(s => s.identifier == sectionIdentifier) match {
      case Some(s: Section) => s
      case _ => Section(sectionIdentifier, List())
    }
  }

  def previousSection(sectionIdentifier: Identifier): Section = {
    sections.filter(s => s.identifier.index < sectionIdentifier.index && s.visible).lastOption match {
      case Some(s: Section) => s
      case _ => section(sectionIdentifier)
    }
  }

  def previousSection(questionGroupIdentifier: QGIdentifier): Section = previousSection(Section.sectionIdentifier(questionGroupIdentifier))

  def questionGroup(questionGroupIdentifier: QGIdentifier): Option[QuestionGroup] = {
    val si = Section.sectionIdentifier(questionGroupIdentifier)
    section(si).questionGroup(questionGroupIdentifier)
  }

  def questionGroup(clazz: Class[_]): Option[QuestionGroup] = {
    sections.flatMap(_.questionGroups).find(_.getClass == clazz) match {
      case Some(q: QuestionGroup) => Some(q)
      case _ => None
    }
  }

  def questionGroup[Q <: QuestionGroup](implicit classTag: ClassTag[Q]): Option[Q] = {

    sections.flatMap(_.questionGroups).find(_.getClass == classTag.runtimeClass) match {
      case Some(q: Q) => Some(q)
      case _ => None
    }
  }

  def previousQuestionGroup(questionGroupIdentifier: QGIdentifier): Option[QuestionGroup] = completedQuestionGroups(questionGroupIdentifier)
    .lastOption

  def completedQuestionGroups(sectionIdentifier: Identifier): List[QuestionGroup] = section(sectionIdentifier).questionGroups

  def completedQuestionGroups(questionGroupIdentifier: QGIdentifier): List[QuestionGroup] = {
    val si = Section.sectionIdentifier(questionGroupIdentifier)
    section(si).precedingQuestionGroups(questionGroupIdentifier)
  }

  def update(section: Section): Claim = {
    val updatedSections = sections.takeWhile(_.identifier.index < section.identifier.index) :::
      List(section) :::
      sections.dropWhile(_.identifier.index <= section.identifier.index)

    copy(sections = updatedSections.sortWith(_.identifier.index < _.identifier.index))
  }

  def +(section: Section): Claim = update(section)

  def update(questionGroup: QuestionGroup): Claim = {
    val si = Section.sectionIdentifier(questionGroup.identifier)
    update(section(si).update(questionGroup))
  }

  def update(saveForLaterPageData: Map[String, String]): Claim = {
    copy(saveForLaterCurrentPageData = saveForLaterPageData)
  }

  def update(newkey: String): Claim = {
    copy(key = newkey)
  }

  /**
   * removes all question groups except the specified ones
   * @param keep - the sections to keep
   */
  def removeQuestionGroups(si: Identifier, keep: Set[QGIdentifier]): Claim = {
    val currentSection = section(si)
    val updatedSection = currentSection.copy(questionGroups = currentSection.questionGroups.filter(qg => keep.contains(qg.identifier)))
    update(updatedSection)
  }

  def +(questionGroup: QuestionGroup): Claim = update(questionGroup)

  def delete(questionGroupIdentifier: QGIdentifier): Claim = {
    val sectionIdentifier = Section.sectionIdentifier(questionGroupIdentifier)
    update(section(sectionIdentifier).delete(questionGroupIdentifier))
  }

  def -(questionGroupIdentifier: QGIdentifier): Claim = delete(questionGroupIdentifier)

  def hideSection(sectionIdentifier: Identifier): Claim = update(section(sectionIdentifier).hide)

  def showSection(sectionIdentifier: Identifier): Claim = update(section(sectionIdentifier).show)

  def showHideSection(visible: Boolean, sectionIdentifier: Identifier): Claim = {
    if (visible) showSection(sectionIdentifier) else hideSection(sectionIdentifier)
  }

  def dateOfClaim: Option[DayMonthYear] = questionGroup(ClaimDate) match {
    case Some(c: ClaimDate) => Some(c.dateOfClaim)
    case _ => None
  }

  def withTransactionId(transactionID: String): Claim = {
    copy(transactionId = Some(transactionID))
  }

  /**
   * Used by submission cache to detect duplicated claims.
   */
  def getFingerprint: String = "f" + this.uuid

  def getEncryptedUuid: String = ClaimEncryption.encryptUuid(uuid)

  def getDecryptedUuid(encuuid: String): String = ClaimEncryption.decryptUuid(encuuid)

}
