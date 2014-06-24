$(function () {
    if (!("WebSocket" in window)) {
        alert("maaan, do you live in the stone ages? get a modern browser!");
        return;
    }
    var socket;

    var ui = {
        settings: {
            container: $('#settings'),
            host: null,
            port: 0,
            ssl: false
        },
        screen: $('#screen'),
        screenContent: $('<div>'),
        commandline: $('#input').find('input'),
        connect: null
    };
    ui.settings.host = ui.settings.container.find('> input[type=text]');
    ui.settings.port = ui.settings.container.find('> input[type=number]');
    ui.settings.ssl = ui.settings.container.find('> input[type=checkbox]');
    ui.screenContent.appendTo(ui.screen);
    ui.connect = ui.settings.container.find('> button');

    var settings = {
        host: function () {
            return ui.settings.host.val();
        },
        port: function () {
            return parseInt(ui.settings.port.val());
        },
        ssl: function () {
            return ui.settings.ssl.prop('checked');
        }
    };

    ui.connect.on('click', function () {
        if (socket) {
            disconnect();
        } else {
            connect();
        }
    });

    function disconnect() {
        if (socket && socket instanceof WebSocket) {
            socket.close();
            socket = null;
        }
        ui.connect.text('reconnect');
    }

    function sendMessage(message) {
        var json = {};
        json.command = message.substring(0, message.indexOf(":"));
        var data = message.substring(message.indexOf(":") + 1);
        try {
            json.data = JSON.parse(data);

        }
        catch (err) {
            print('error', "Cannot parse: " + message);
            return 0;
        }
        socket.send(JSON.stringify(json));
        print('written', JSON.stringify(json));
    }

    function print(type, message) {
        var scroll = Math.abs(ui.screen.scrollTop() - (ui.screenContent.outerHeight() - ui.screen.outerHeight())) < 5;
        var line = $('<div class="message-' + type + '">');
        line.text(message);
        line.appendTo(ui.screenContent);

        if (scroll) {
            ui.screen.scrollTop(ui.screenContent.outerHeight() - ui.screen.outerHeight());
        }
    }

    function connect() {
        if (socket && socket.readyState == WebSocket.OPEN) {
            return;
        }
        var url = (settings.ssl() ? 'wss' : 'ws') + '://' + settings.host() + ':' + settings.port() + '/websocket';
        socket = new WebSocket(url + location.search);

        socket.onopen = function () {
            print('status', 'Connected!');
            ui.commandline.prop('disabled', false);
            ui.connect.text("disconnect");
        };
        socket.onmessage = function (e) {
            print('read', e.data);
        };
        socket.onerror = function () {
            print('error', 'Error');
        };
        socket.onclose = function () {
            print('status', 'Disconnected!');
            disconnect();
        };

        ui.connect.text("connecting...");
    }

    var history = JSON.parse(localStorage.getItem("history") || "[]");
    var historyIndex = history.length;

    ui.commandline.on('keydown', function (e) {
        var c = e.keyCode;
        if (c == 13) {
            var message = $.trim(ui.commandline.val());
            if (message.length > 0) {
                sendMessage(message);
                if (history.length == 0 || history[history.length - 1] != message) {
                    history.push(message);
                }
                historyIndex = history.length;
                ui.commandline.val('');
                localStorage.setItem("history", JSON.stringify(history));
            }
        } else if (history.length > 0) {
            if (c == 38 && historyIndex > 0) {
                ui.commandline.val(history[--historyIndex]);
            } else if (c == 40 && historyIndex < history.length) {
                ui.commandline.val(history[++historyIndex]);
            }
        }
    });

    ui.settings.container.on('keydown', function(e) {
        if (e.keyCode == 13) {
            connect();
        }
    });
});
