package xml

import app.XMLValues
import models.domain._
import XMLHelper.{stringify}
import scala.xml.NodeSeq
import app.XMLValues._

object Partner {

  def xml(claim: Claim) = {
    val moreAboutYou = claim.questionGroup[MoreAboutYou].getOrElse(MoreAboutYou(hadPartnerSinceClaimDate = no))

    val yourPartnerPersonalDetails = claim.questionGroup[YourPartnerPersonalDetails].getOrElse(YourPartnerPersonalDetails())

    val personYouCareFor = claim.questionGroup[PersonYouCareFor].getOrElse(PersonYouCareFor())

    val hadPartner = moreAboutYou.hadPartnerSinceClaimDate == yes

    if (hadPartner) {
      <Partner>
        <Surname>{yourPartnerPersonalDetails.surname}</Surname>
        <OtherNames>{yourPartnerPersonalDetails.firstName} {yourPartnerPersonalDetails.middleName.orNull}</OtherNames>
        {yourPartnerPersonalDetails.otherSurnames match {
          case Some(n) => <OtherSurnames>{yourPartnerPersonalDetails.otherSurnames.orNull}</OtherSurnames>
          case None => NodeSeq.Empty
        }}
        <Title>{yourPartnerPersonalDetails.title}</Title>
        <DateOfBirth>{yourPartnerPersonalDetails.dateOfBirth.`dd-MM-yyyy`}</DateOfBirth>
        <NationalInsuranceNumber>{stringify(yourPartnerPersonalDetails.nationalInsuranceNumber)}</NationalInsuranceNumber>
        <NationalityPartner>{yourPartnerPersonalDetails.nationality.orNull}</NationalityPartner>
        <RelationshipStatus>
          <SeparatedFromPartner>
            <QuestionLabel>yourPartnerPersonalDetails.separatedFromPartner?</QuestionLabel>
            <Answer>{yourPartnerPersonalDetails.separatedFromPartner match {
              case "yes" => XMLValues.Yes
              case "no" => XMLValues.No
              case n => n
            }}</Answer>
          </SeparatedFromPartner>
        </RelationshipStatus>
        {personYouCareFor.isPartnerPersonYouCareFor.isEmpty match {
        case false =>
          <IsCaree>
            <QuestionLabel>IsCaree?</QuestionLabel>
            <Answer>{personYouCareFor.isPartnerPersonYouCareFor match {
              case "yes" => XMLValues.Yes
              case "no" => XMLValues.No
              case n => n
            }}</Answer>
          </IsCaree>
        case true => NodeSeq.Empty
      }}
      </Partner>
    } else NodeSeq.Empty
  }
}
