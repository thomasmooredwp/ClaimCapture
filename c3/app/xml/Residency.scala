package xml

import app.XMLValues._
import models.domain._
import models.yesNo.YesNoWithDate
import scala.xml.NodeSeq
import xml.XMLHelper.stringify

object Residency {

  def xml(claim: Claim) = {
    val yourDetailsOption = claim.questionGroup[YourDetails]
    val normalResidence = claim.questionGroup[NormalResidenceAndCurrentLocation].getOrElse(NormalResidenceAndCurrentLocation())
    val tripsOption = claim.questionGroup[Trips]

    <Residency>
      <Nationality>{/*if (yourDetailsOption.isDefined)yourDetailsOption.get.nationality*/ /*TODO: Fix this*/}</Nationality>
      <EUEEASwissNational>{NotAsked}</EUEEASwissNational>
      <CountryNormallyLive>{normalResidence.whereDoYouLive.text.getOrElse(NotAsked)}</CountryNormallyLive>
      <CountryNormallyLiveOther>{NotAsked}</CountryNormallyLiveOther>
      <InGreatBritainNow>{normalResidence.inGBNow}</InGreatBritainNow>
      <InGreatBritain26Weeks>{NotAsked}</InGreatBritain26Weeks>
      {periodAbroadLastYear(tripsOption)}
      <BritishOverseasPassport>{NotAsked}</BritishOverseasPassport>
      {otherNationality(claim)}
      <OutOfGreatBritain>{NotAsked}</OutOfGreatBritain>
      {periodAbroadDuringCare(tripsOption)}
    </Residency>
  }

  def periodAbroadLastYear(tripsOption: Option[Trips]) = {
    val trips = tripsOption.getOrElse(Trips())

    def xml(trip: TripPeriod) = {
      <PeriodAbroadLastYear>
        <Period>
          <DateFrom>{trip.start.`yyyy-MM-dd`}</DateFrom>
          <DateTo>{trip.end.`yyyy-MM-dd`}</DateTo>
        </Period>
        <Reason>{trip.why}</Reason>
        <Country>{trip.where}</Country>
      </PeriodAbroadLastYear>
    }

    {for {fourWeeksTrip <- trips.fourWeeksTrips} yield xml(fourWeeksTrip)}
  }

  def otherNationality(claim:Claim) = {
    val timeOutsideUKOption = claim.questionGroup[TimeOutsideUK]
    val timeOutsideUK = timeOutsideUKOption.getOrElse(TimeOutsideUK())
    val currentlyLivingInUK = timeOutsideUK.livingInUK.answer == yes
    if(currentlyLivingInUK) {
      val goBack = timeOutsideUK.livingInUK.goBack.getOrElse(YesNoWithDate("", None))
      <OtherNationality>
        <EUEEASwissNationalChildren/>
        <DateArrivedInGreatBritain>{NotAsked}</DateArrivedInGreatBritain>
        <CountryArrivedFrom>{timeOutsideUK.livingInUK.text.orNull}</CountryArrivedFrom>
        <IntendToReturn>{goBack.answer}</IntendToReturn>
        <DateReturn>{stringify(goBack.date)}</DateReturn>
        <VisaReferenceNumber>{NotAsked}</VisaReferenceNumber>
      </OtherNationality>

    } else NodeSeq.Empty
  }

  def periodAbroadDuringCare(tripsOption: Option[Trips]) = {
    val trips = tripsOption.getOrElse(Trips())

    def xml(trip: TripPeriod) = {
      <PeriodAbroadDuringCare>
        <Period>
          <DateFrom>{trip.start.`yyyy-MM-dd`}</DateFrom>
          <DateTo>{trip.end.`yyyy-MM-dd`}</DateTo>
        </Period>
        <Reason>{trip.why}</Reason>
      </PeriodAbroadDuringCare>
    }

    {for {fiftyTwoWeeksTrip <- trips.fiftyTwoWeeksTrips} yield xml(fiftyTwoWeeksTrip)}
  }
}