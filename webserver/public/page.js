var driverColors = ['red', 'green', 'blue', 'indigo', 'violet'];
// socket.io connection
const socket = io({
    transports: ['websocket']
});

// on page loaded
document.addEventListener('DOMContentLoaded', (event) => {

    // create a leaflet map object
    var mymap = L.map('mapid').setView([47.610664, -122.338917], 14);
    // save the markers representing the drivers
    var driverMarkers = {};
    // configure with tile server
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        ext: 'png',
        maxZoom: 18,
        attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
    }).addTo(mymap);

    // when you get a message from the node socket.io server
    socket.on('new message', (data) => {
        if (!(data.key in driverMarkers)) {
            var colorPos = Object.keys(driverMarkers).length % driverColors.length;
            var color = driverColors[colorPos];
            driverMarkers[data.key] = L.circleMarker([data.latitude, data.longitude], {
                radius: 5,
                color: color,
                drivertype: "live",
                title: data.key
            });
            driverMarkers[data.key].addTo(mymap);
            //hljs.highlightBlock(newPre);
        }
        driverMarkers[data.key].setLatLng([data.latitude, data.longitude]);

        var newPre = document.createElement("pre");
        newPre.setAttribute("id", data.key);
        var newCode = document.createElement("code");
        newCode.appendChild(document.createTextNode(JSON.stringify(data, null, 2)));
        newPre.appendChild(newCode);
        var jsonNode = document.getElementById('json');
        if (document.getElementById(data.key)) {
            jsonNode.replaceChild(newPre, document.getElementById(data.key));
        } else {
            jsonNode.append(newPre);
        }
        hljs.highlightBlock(newPre);
    });

    document.getElementById("view").addEventListener("click", function () {
        // cycle thru and remove all previous history markers
        mymap.eachLayer(function (layer) {
            if (layer instanceof L.CircleMarker) {
                if (layer.options.drivertype === 'history') {
                    layer.remove();
                }
            }
        });

        // cycle through all the lines in the textbox
        var lines = $('#manual').val().split('\n');
        var latlngs = [];
        for (i in lines) {
            // the lines should be in the format of driver-id,latitude,longitude
            var arr = lines[i].split(",");
            if (arr.length > 2) {
                var driverId = arr[0];
                var latitude = parseFloat(arr[1]);
                var longitude = parseFloat(arr[2]);
                if (!isNaN(latitude) && !isNaN(longitude)) {
                    var color = (driverId in driverMarkers) ? driverMarkers[driverId].options.color : 'red';
                    L.circleMarker([latitude, longitude], {
                        radius: 5,
                        color: color,
                        drivertype: "history"
                    }).addTo(mymap);
                }
            }
        }
    });
});