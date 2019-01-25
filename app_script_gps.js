function doGet(e) {
    const param = e.parameter;
    const to = param.to;
    const mapurl = "https://www.google.com/maps/place/" + param.lat +","+param.lng;
  MailApp.sendEmail(to,
                      "GPS report",
                      "Event happens at: \n" + mapurl);
    const reply = "email sent to:" + to +" with location ("+param.lat+ ", "+ param.lng + ")";
    return ContentService.createTextOutput(reply);
}

