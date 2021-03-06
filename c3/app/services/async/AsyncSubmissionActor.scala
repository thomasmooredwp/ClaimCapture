package services.async

import play.api.Play._

import scala.language.postfixOps
import scala.concurrent.duration._
import akka.actor.{Props, OneForOneStrategy, SupervisorStrategy, Actor}
import models.domain.Claim
import services.submission.AsyncClaimSubmissionComponent
import akka.actor.SupervisorStrategy.Restart
import play.api.Logger

class AsyncSubmissionActor extends Actor {

  val asyncSubmissionService = current.injector.instanceOf[AsyncClaimSubmissionComponent]

  override def receive: Actor.Receive = {
    case claim:Claim =>
      Logger.info(s"Processing ${claim.key} ${claim.uuid} with transactionId [${claim.transactionId.get}]")
      asyncSubmissionService.submission(claim)
  }
}



class AsyncSubmissionManagerActor(childActorProps:Props) extends Actor {


  override def supervisorStrategy: SupervisorStrategy = OneForOneStrategy(maxNrOfRetries = 2, withinTimeRange = 30 seconds){
    case e:Exception =>
      Logger.error("Need to restart AsyncSubmissionManagerActor.",e)
      Restart
  }

  override def receive: Actor.Receive = {
    case claim:Claim =>
      Logger.info(s"Received ${claim.key} ${claim.uuid} with transactionId [${claim.transactionId.get}]")
      context.actorOf(childActorProps) ! claim
  }
}
