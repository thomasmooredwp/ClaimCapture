package models.domain

import models._
import controllers.mappings.Mappings._

object Education extends Identifier(id = "s8")

case class YourCourseDetails(beenInEducationSinceClaimDate: String = "",
                             title: Option[String]  = None,
                             nameOfSchoolCollegeOrUniversity: Option[String] = None,
                             nameOfMainTeacherOrTutor: Option[String] = None,
                             courseContactNumber: Option[String] = None,
                             startDate: Option[DayMonthYear] = None,
                             expectedEndDate: Option[DayMonthYear] = None
                            ) extends QuestionGroup(YourCourseDetails)

object YourCourseDetails extends QGIdentifier(id = s"${Education.id}.g1") {

  def validateTitle(input: YourCourseDetails): Boolean = input.beenInEducationSinceClaimDate match {
    case `yes` => input.title.isDefined
    case `no` => true
  }

  def validateNameOfSchool(input: YourCourseDetails): Boolean = input.beenInEducationSinceClaimDate match {
    case `yes` => input.nameOfSchoolCollegeOrUniversity.isDefined
    case `no` => true
  }

  def validateNameOfTeacher(input: YourCourseDetails): Boolean = input.beenInEducationSinceClaimDate match {
    case `yes` => input.nameOfMainTeacherOrTutor.isDefined
    case `no` => true
  }

  def validateStartDate(input: YourCourseDetails): Boolean = input.beenInEducationSinceClaimDate match {
    case `yes` => input.startDate.isDefined
    case `no` => true
  }

  def validateExpectedEndDate(input: YourCourseDetails): Boolean = input.beenInEducationSinceClaimDate match {
    case `yes` => input.expectedEndDate.isDefined
    case `no` => true
  }

}
