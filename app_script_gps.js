function doGet(e) {
    var param = e.parameter;
    const to = param.to;
    const mapurl = "https://www.google.com/maps/place/" + param.lat +","+param.lng;
  MailApp.sendEmail(to,
                      "GPS report",
                      "Event happens at: \n" + mapurl);
    return ContentService.createTextOutput("email sent");
}

