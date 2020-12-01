document.addEventListener('deviceready', onDeviceReady, false);

function onDeviceReady() {

    debugconsole.start("log");

    debugconsole.log('Running cordova-' + cordova.platformId + '@' + cordova.version);
    document.getElementById('deviceready').classList.add('ready');

    document.querySelector("#debug_console > h2").parentElement.classList.add("movedown");

    WifiHotspot.hello("App active!");

    document.getElementById("perm_chek_button").onclick = function() {
        WifiHotspot.chekPermission(function(e){
          debugconsole.log("Hotspot chekPermission - done -" + e);
          WifiHotspot.hello("Access allowed!");
        }, function(e) {
          debugconsole.log("Hotspot chekPermission - done -" + e);
        });
    }

    document.getElementById("hotspot_start").onclick = function() {

        var ssid = document.getElementById("hs_ssid").value;
        var pswd = document.getElementById("hs_pswd").value;

        if(!ssid || !pswd || pswd.length < 8) {
            alert("Please, enter SSID name and password! \n Password must be longer then 8 num.");
            debugconsole.log("SSID or password error!");
            return false;
        }

        WifiHotspot.setup(ssid, pswd, true, false, function(e){
          debugconsole.log("Hotspot setup - done -" + e);
          WifiHotspot.switchAPon(function(e){
            debugconsole.log("Hotspot configApState - done -" + e);
          }, function(e) {
            debugconsole.log("Hotspot configApState - done -" + e);
          });
        }, function(e) {
          debugconsole.log("Hotspot setup - error -" + e);
        });

    }

    document.getElementById("hotspot_stop").onclick = function() {
        WifiHotspot.switchAPoff(function(e){
          debugconsole.log("Hotspot switchAPoff - done -" + e);
          WifiHotspot.hello("Hotspot stoped!");
        }, function(e) {
          debugconsole.log("Hotspot switchAPoff - done -" + e);
        });
    }
}
