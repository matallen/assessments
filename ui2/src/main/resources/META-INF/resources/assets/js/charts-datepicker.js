
const monthNames = ["January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"];
var now=new Date();
var currentMonth=new Date(now.getFullYear(), now.getMonth(), 1);
var lastMonth=new Date(currentMonth.getFullYear(), currentMonth.getMonth()-1, 1);
function dateMinusMonths(d, minusMonths){
	return new Date(d.getFullYear(), d.getMonth()-minusMonths, d.getDate());
}
function dateToYYYYMMM(d){
	return d.getFullYear()+"-"+monthNames[d.getMonth()].slice(0,3);
}
function getCYYear(d){
	return (d.getFullYear()+"").slice(2,4);
}
function getCYQ(d){
	return "Q"+Math.ceil((d.getMonth()+1)/3); // ie. 1-3=1, 4-6=2, 7-9=3 & 10-12=4
}
function monthNumerialToStartOfQForCY(d){
	if (d.getMonth()==1 || d.getMonth()==4 || d.getMonth()==7 ||d.getMonth()==10) return 1;  
	if (d.getMonth()==2 || d.getMonth()==5 || d.getMonth()==8 ||d.getMonth()==11) return 2;
	return 0;
}
function monthNumerialToEndOfQForCY(d){
	if (d.getMonth()==1 || d.getMonth()==4 || d.getMonth()==7 ||d.getMonth()==10) return 1;  
	if (d.getMonth()==0 || d.getMonth()==3 || d.getMonth()==6 ||d.getMonth()==9) return 2; 
	return 0;
}
var dateThisQStart=dateMinusMonths(currentMonth, monthNumerialToStartOfQForCY(now));
var dateThisQEnd=dateMinusMonths(currentMonth, -1*monthNumerialToEndOfQForCY(now));

function generateDateRangeSelect(name, selected){
	var currentMonthYYYYMMM=dateToYYYYMMM(currentMonth);
	var lastMonthYYYYMMM=dateToYYYYMMM(lastMonth);
	
	var endMonth=currentMonth;
	var endMonthYYYYMMM=currentMonthYYYYMMM;
	
	var targetEndMonthYYYYMMM=dateToYYYYMMM(endMonth);
	var minus3months=dateToYYYYMMM(dateMinusMonths(endMonth,2));
	var minus6months=dateToYYYYMMM(dateMinusMonths(endMonth,5));
	var minus9months=dateToYYYYMMM(dateMinusMonths(endMonth,8));
	var thisQuarterStart=dateToYYYYMMM(dateThisQStart);
	var thisQuarterEnd=dateToYYYYMMM(dateThisQEnd);
	var thisQuarterLabel="CY"+getCYYear(dateThisQStart)+" "+getCYQ(dateThisQStart)+" ("+thisQuarterStart+" -> "+thisQuarterEnd+")";
	var lastQuarterStart=dateToYYYYMMM(dateMinusMonths(dateThisQStart,3));
	var lastQuarterEnd=dateToYYYYMMM(dateMinusMonths(dateThisQEnd,3));
	var lastQuarterLabel="CY"+getCYYear(dateMinusMonths(dateThisQStart,3))+" "+getCYQ(dateMinusMonths(dateThisQStart,3))+" ("+lastQuarterStart+" -> "+lastQuarterEnd+")";
	if (null==selected) selected=thisQuarterLabel;// default to current Quarter

	var result="<select id='"+name+"Months' style='color: #333' onchange='"+name+"refresh(); return false;'>";
	result+="<option "+(selected==thisQuarterLabel?"selected":"")+" value='start="+thisQuarterStart+"&end="+thisQuarterEnd+"' data-start='"+thisQuarterStart+"' data-end='"+thisQuarterEnd+"'>"+thisQuarterLabel+"</option>";
	result+="<option "+(selected==lastQuarterLabel?"selected":"")+" value='start="+lastQuarterStart+"&end="+lastQuarterEnd+"' data-start='"+lastQuarterStart+"' data-end='"+lastQuarterEnd+"'>"+lastQuarterLabel+"</option>";
	result+="<option "+(selected=="Current Month"?"selected":"")+" value='start="+currentMonthYYYYMMM+"&end="+currentMonthYYYYMMM+"' data-start='"+currentMonthYYYYMMM+"' data-end='"+currentMonthYYYYMMM+"'>Current Month</option>";
	result+="<option "+(selected=="Last Month"?"selected":"")+" value='start="+lastMonthYYYYMMM+"&end="+lastMonthYYYYMMM+"' data-start='"+lastMonthYYYYMMM+"' data-end='"+lastMonthYYYYMMM+"'>Last Month</option>";
	result+="<option "+(selected=="3 Months"?"selected":"")+"   value='start="+minus3months+"&end="+    endMonthYYYYMMM+"' data-start='"+minus3months+"'     data-end='"+endMonthYYYYMMM+"'>3 Months</option>";
	result+="<option "+(selected=="6 Months"?"selected":"")+"   value='start="+minus6months+"&end="+    endMonthYYYYMMM+"' data-start='"+minus6months+"'     data-end='"+endMonthYYYYMMM+"'>6 Months</option>";
	result+="<option "+(selected=="9 Months"?"selected":"")+"   value='start="+minus9months+"&end="+    endMonthYYYYMMM+"' data-start='"+minus9months+"'     data-end='"+endMonthYYYYMMM+"'>9 Months</option>";
//	result+="<option "+(selected=="9 Months (incl Current)"?"selected":"")+"   value='start="+minus9months+"&end="+currentMonthYYYYMMM+"'     data-start='"+minus9months+"'     data-end='"+currentMonthYYYYMMM+"'>9 Months (incl Current)</option>";
	return result+"</select>";
}