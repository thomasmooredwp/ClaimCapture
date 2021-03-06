window.initEvents = (educationY, educationN,
                     courseTitle, nameOfSchoolCollegeOrUniversity,nameOfMainTeacherOrTutor,
                     courseContactNumber,startDateDay, startDateMonth, startDateYear,
                     expectedEndDateDay, expectedEndDateMonth, expectedEndDateYear) ->

  if not $("#" + educationY).prop('checked')
    hideEducationWrap(courseTitle, nameOfSchoolCollegeOrUniversity,nameOfMainTeacherOrTutor,
      courseContactNumber,startDateDay, startDateMonth, startDateYear,
      expectedEndDateDay, expectedEndDateMonth, expectedEndDateYear)

  $("#" + educationY).on "click", ->
    showEducationWrap()

  $("#" + educationN).on "click", ->
    hideEducationWrap(courseTitle, nameOfSchoolCollegeOrUniversity,nameOfMainTeacherOrTutor,
      courseContactNumber,startDateDay, startDateMonth, startDateYear,
      expectedEndDateDay, expectedEndDateMonth, expectedEndDateYear)

showEducationWrap = ->
  $("#educationWrap").slideDown(0).attr 'aria-hidden', 'false'

hideEducationWrap = (courseTitle, nameOfSchoolCollegeOrUniversity,nameOfMainTeacherOrTutor,
  courseContactNumber,startDateDay, startDateMonth, startDateYear,
  expectedEndDateDay, expectedEndDateMonth, expectedEndDateYear) ->
	  
	emptyEducation = ->
	    $("#" + courseTitle).val("")
	    $("#" + nameOfSchoolCollegeOrUniversity).val("")
	    $("#" + nameOfMainTeacherOrTutor).val("")
	    $("#" + courseContactNumber).val("")
	    $("#" + startDateDay).val("")
	    $("#" + startDateMonth).val("")
	    $("#" + startDateYear).val("")
	    $("#" + expectedEndDateDay).val("")
	    $("#" + expectedEndDateMonth).val("")
	    $("#" + expectedEndDateYear).val("")
  $("#educationWrap").slideUp(0, emptyEducation).attr 'aria-hidden', 'true', ->