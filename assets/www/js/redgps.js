document.addEventListener("deviceready", onDeviceReady, false);
var locationService = cordova.require('cordova/plugin/locationService');
var timeSet = 0;
var reporting = null;
var db = null;
var serverName = '';
var serverPort = 0;
var started = false;
var running = false;
var onSuccessLocation = function(position) {
	alert('Latitude: '          + position.coords.latitude          + '\n' +
  	'Longitude: '         + position.coords.longitude         + '\n' +
  	'Altitude: '          + position.coords.altitude          + '\n' +
  	'Accuracy: '          + position.coords.accuracy          + '\n' +
  	'Altitude Accuracy: ' + position.coords.altitudeAccuracy  + '\n' +
  	'Heading: '           + position.coords.heading           + '\n' +
  	'Speed: '             + position.coords.speed             + '\n' +
  	'Timestamp: '         + position.timestamp                + '\n');
};

function onDeviceReady() {
    db = window.openDatabase("redgps_conf", "1.0", "BD de Configuracion RedGPS", 1000000);
    db.transaction(createDatabase, errorCB, successCB);	
    db.transaction(getTimeSetFromDb, errorCB, successCB);
    db.transaction(getServerName, errorCB, successCB);
    document.getElementById('imei').value = device.uuid;
}

function createDatabase(tx) {
	tx.executeSql('CREATE TABLE IF NOT EXISTS CONFIG (key unique, value)');
}

function errorCB(err) {
	alert("Error processing SQL: "+ err.message);
}

function successCB() {
	
}

function getTimeSetFromDb(tx) {
	tx.executeSql('SELECT * FROM CONFIG WHERE key = "TIME_SET"', [], queryTimeSetSuccess, errorCB);
}

function getServerName(tx) {
	tx.executeSql('SELECT * FROM CONFIG WHERE key = "SERVER_NAME"', [], function(tx, results) {
		var len = results.rows.length;
		if (len > 0) {
			serverName = results.rows.item(0).value;
			document.getElementById("txtServer").value = serverName; 
		}
	}, errorCB);
	tx.executeSql('SELECT * FROM CONFIG WHERE key = "SERVER_PORT"', [], function(tx, results) {
		var len = results.rows.length;
		if (len > 0) {
			serverPort = parseInt(results.rows.item(0).value);
			document.getElementById("txtPort").value = serverPort;
		}
		getStatus();
	}, errorCB);
}

function queryTimeSetSuccess(tx, results) {
	var len = results.rows.length;
    if (len > 0) {
    	timeSet = parseInt(results.rows.item(0).value);
    } else {
    	tx.executeSql('INSERT INTO CONFIG(key, value) VALUES("TIME_SET", "5")');
    	timeSet = 5;
    }
    setReporting();
}

function setReporting() {
	if (reporting) {
		clearInterval(reporting);
	}
	var seconds = timeSet * 60000;
	reporting = setInterval(function(){
		navigator.geolocation.getCurrentPosition(onSuccessLocation, onErrorLocation);
	}, seconds);
}

function onErrorLocation(error) {
    alert('code: '    + error.code    + '\n' +
          'message: ' + error.message + '\n');
}

function handleSuccess(data, close) {
    if (close) {
    	navigator.app.exitApp();
    } else {
		updateView(data);
	}
}

function handleError(data) {
	alert("Error: " + data.ErrorMessage);
	alert(JSON.stringify(data));
	updateView(data);
}
/*
* Button Handlers
*/
function getStatus() {
	locationService.getStatus(function(r){ handleSuccess(r); },
		function(e){handleError(e);});
};
 
function startService() {
	locationService.startService(	function(r){setConfig()},
		function(e){handleError(e)});
}

function stopService(close) {
	locationService.stopService(function(r){handleSuccess(r, close)},
		function(e){handleError(e)});
}

function enableTimer() {
	var newTimeSet = 0;
	try {
		newTimeSet = parseInt($('select#txtTimer option:selected').val());
	} catch(err) {
		alert("The time set value is not valid");
	}
	
	if (newTimeSet != timeSet && newTimeSet > 0) {
		timeSet = newTimeSet;
		db.transaction(function(tx) {
			tx.executeSql('UPDATE CONFIG SET value = "' + timeSet + '" WHERE key = "TIME_SET"');
			setReporting();
			}, errorCB, successCB);
	} else if (newTimeSet <= 0) {
		disableTimer();
		return;
	}
	locationService.enableTimer(timeSet * 60000,
		function(r){handleSuccess(r)},
		function(e){handleError(e)});
}


function disableTimer() {
	locationService.disableTimer(function(r){handleSuccess(r)},
		function(e){handleError(e)});
	if (reporting) {
		clearInterval(reporting);
	}
};

function registerForBootStart() {
	locationService.registerForBootStart(function(r){handleSuccess(r)},
		function(e){handleError(e)});
}

function deregisterForBootStart() {
	locationService.deregisterForBootStart(function(r){handleSuccess(r)},
		function(e){handleError(e)});
}

function setConfig() {
	var server = $("#txtServer").val();
  	var port = $("#txtPort").val();
  	if (server == '') {
  		alert("El nombre de servidor no puede estar vacio");
  		return;
  	}
  	var config = {
  		"server_ip": server,
  		"port": port,
  	};
  	db.transaction(function(tx) {
  		if (serverName != '') {
  			db.transaction(function(c) {
  				c.executeSql('UPDATE CONFIG SET value = "' + server + '" WHERE key = "SERVER_NAME"');
  			}, errorCB, successCB);
  		} else {
  			db.transaction(function(c) {
  				c.executeSql('INSERT INTO CONFIG(key, value) VALUES("SERVER_NAME", "' + server + '")');
  			}, errorCB, successCB);
  		}
  		serverName = server;
  	}, errorCB, successCB);
  	
  	db.transaction(function(tx) {
  		if (serverPort != 0) {
  			db.transaction(function(c) {
  				c.executeSql('UPDATE CONFIG SET value = "' + port + '" WHERE key = "SERVER_PORT"');
  			}, errorCB, successCB);
  		} else {
  			db.transaction(function(c) {
  				c.executeSql('INSERT INTO CONFIG(key, value) VALUES("SERVER_PORT", "' + port + '")');
  			}, errorCB, successCB);
  		}
  		serverPort = parseInt(port);
  	}, errorCB, successCB);
  	
	locationService.setConfiguration(config,
		function(r){handleSuccess(r)},
		function(e){handleError(e)});
}

/*
* View logic
*/
function updateView(data) {
	var serviceBtn = document.getElementById("toggleService");
	var timerBtn = document.getElementById("toggleTimer");
	var txtTimer = document.getElementById("txtTimer");
	var bootBtn = document.getElementById("toggleBoot");
	var updateBtn = document.getElementById("updateBtn");
	var refreshBtn = document.getElementById("refreshBtn");
	
	
	txtTimer.onchange = enableTimer;
	if (data.TimerEnabled) {
		$('select#txtTimer option').each(function() {
			if (parseInt($(this).val()) == timeSet) {
				$(this).attr('selected', 'selected');
			} 
		});
	}
	
	if (data.RegisteredForBootStart) {
		bootBtn.onchange = deregisterForBootStart;
		$('select#toggleBoot option[value="0"]').removeAttr('selected');
		$('select#toggleBoot option[value="1"]').attr('selected', 'selected');
	} else {
		bootBtn.onchange = registerForBootStart;
		$('select#toggleBoot option[value="1"]').removeAttr('selected');
		$('select#toggleBoot option[value="0"]').attr('selected', 'selected');
	}
	
	if (data.LatestResult != null)
	{
		try {
			var resultMessage = document.getElementById("ultimo_reporte");
			resultMessage.innerHTML = data.LatestResult.Message;
		} catch (err) {}
	}
	if (!running) {
		alert("Bienvenido a RedGps\n" +
        'Para utiliar esta aplicación deberá configurar el servidor y luego el tiempo de reporte ' +
        'luego de esto, usted podrá ya visualizar este equipo celular en la plataforma de www.redgps.com');
        running = true;
	}
	if(data.TimerEnabled && data.ServiceRunning) {
		$('#status_reportando').show();
        $('#ultimo_reporte').show();
        $('#status_detenido').hide();
	} else {
		$('#status_detenido').show();
        $('#status_reportando').hide();
        $('#ultimo_reporte').hide();
	}
	document.getElementById('txtServer').value = serverName;
	document.getElementById('txtPort').value = serverPort;
}