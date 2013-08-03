package xml

import models.{NationalInsuranceNumber, MultiLineAddress, DayMonthYear}
import scala.xml.NodeBuffer

object XMLHelper {

  def stringify(value: Option[_]): String = value match {
    case Some(s: String) => s
    case Some(dmy: DayMonthYear) => dmy.`yyyy-MM-dd`
    case Some(nr: NationalInsuranceNumber) => nr.toXmlString
    case _ => ""
  }

  def postalAddressStructure(addressOption: Option[MultiLineAddress], postcodeOption: Option[String]): NodeBuffer = addressOption match {
    case Some(address:MultiLineAddress) => postalAddressStructure(address, postcodeOption.orNull)
    case _ =>  postalAddressStructure(MultiLineAddress(None, None, None), postcodeOption.orNull)
  }

  def postalAddressStructure(address: MultiLineAddress, postcode: String): NodeBuffer = {
    <gds:Line>{address.lineOne.orNull}</gds:Line>
    <gds:Line>{address.lineTwo.orNull}</gds:Line>
    <gds:Line>{address.lineThree.orNull}</gds:Line>
    <gds:PostCode>{postcode}</gds:PostCode>
  }
}