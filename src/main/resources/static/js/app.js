var app = angular.module('myapp', []);

app.controller('posterController', function($scope, $http, $window){
    $scope.sendPost = function() {
        var postObj = {
            topic: document.getElementById("postTopic").value,
            body: document.getElementById("postBody").value
        }
        $http.post('/post', postObj);
        // TODO provide some confirmation
    }

    $scope.postEntered = function() {
        var topic = document.getElementById("postTopic").value;
        var body = document.getElementById("postBody").value;
        if (topic.length < 3 || body.length < 10) {
            document.getElementById("postButton").disabled = true;
        } else {
            document.getElementById("postButton").disabled = false;
        }
    }
});

app.controller('loginController', function($scope, $http, $window) {
    $scope.init = function() {
        $http.get('/passwordRegistered')
            .then(function successCallback(response) {
                $scope.registered = true; },
                  function errorCallback(response) {
                $scope.registered = false; });

        $http.get('/isAuthorized')
            .then(function successCallback(response) { $window.location.href = "/index.html"; },
                  function errorCallback(response) { } );
    }

    $scope.login = function() {
        if ($scope.registered) {
            var postObj = {
                password: document.getElementById("password").value,
            }
        } else {
            var postObj = {
                password: document.getElementById("newPassword1").value,
            }
        }

        $http.post('/login', postObj)
            .then(function successCallback(response) { $window.location.href = "/index.html"; },
                  function errorCallback(response) { });
    }

    $scope.passwordChanged = function() {
        var passwordLength = document.getElementById("password").value.length;
        if (passwordLength < 8)
            document.getElementById("buttonLogIn").disabled = true;
        else
            document.getElementById("buttonLogIn").disabled = false;
    }

    $scope.newPasswordChanged = function() {
        var password1 = document.getElementById("newPassword1").value;
        var password2 = document.getElementById("newPassword2").value;
        if (password1 != password2 || password1.length < 8 || password2.length < 8)
            document.getElementById("buttonLogIn").disabled = true;
        else
            document.getElementById("buttonLogIn").disabled = false;
    }

    $scope.init();
});

app.controller('credentialsController', function($scope, $http, $window) {
    $scope.initApps = function() {
        $http.get('/apps').then(function successCallback(response) {
            $scope.apps = response.data;
        });
    }

    $scope.deleteApp = function(appName) {
        var deleteRequest = {
            method: 'DELETE',
            url: 'apps',
            headers: {
                'Content-Type': 'text/plain'
            },
            data: appName
        };
        $http(deleteRequest).then(function successCallback(response) {
              $scope.initApps();
        });
    }

    $scope.enableApp = function() {
        var patchObj = {
            appName: this.app.appName,
            enabled: this.app.enabled
        };
        $http.patch('/apps', patchObj);
    }

    $scope.storeCredentials = function() {
        var credentialsObj = {
            appName: document.getElementById("appName").value,
            username: document.getElementById("username").value,
            password: document.getElementById("password").value
        }
        $http.post('/apps', credentialsObj).then(function successCallback(response) {
            $scope.initApps();
            document.getElementById("appName").value = '';
            document.getElementById("username").value = '';
            document.getElementById("password").value = '';
        });
    }

    $scope.authorizeGmail = function() {
        $http.post('/apps/authorize', document.getElementById("email").value)
             .then(function successCallback(response) {
                 $window.open(response.data, 'Google OAuth', 'width=640,height=540');
                 $window.onfocus = function() {
                    // refresh available apps when user ends Google authorization
                    $scope.initApps();
                    document.getElementById("email").value = '';
                    $window.onfocus = null;
                 };
             });
    }

    $scope.wordpressInputChanged = function() {
        var appName = document.getElementById("appName").value;
        var username = document.getElementById("username").value;
        var password = document.getElementById("password").value;
        if (appName && username && password &&
            /^([\da-z-]*)(\.[\da-z-]{2,})*(\.[a-z]{2,6})?(:[\d]{1,5})?(\/[\w\-]+)*$/.test(appName)) {
            document.getElementById("buttonAddWordpress").disabled = false;
        } else {
            document.getElementById("buttonAddWordpress").disabled = true;
        }
    }

    $scope.gmailInputChanged = function() {
        var inputEmail = document.getElementById("email").value;
        if (/^[_A-Za-z0-9-+]+(.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(.[A-Za-z0-9]+)*(.[A-Za-z]{2,})$/.test(inputEmail)) {
            document.getElementById("buttonAddGmail").disabled = false;
        } else {
            document.getElementById("buttonAddGmail").disabled = true;
        }
    }

    $scope.initApps();
});

app.controller('googleController', function($scope, $http, $window, $location) {
    function getParam(param) {
        var vars = {};
        window.location.href.replace( location.hash, '' ).replace(
            /[?&]+([^=&]+)=?([^&]*)?/gi, // regexp
            function( m, key, value ) { // callback
                vars[key] = value !== undefined ? value : '';
            }
        );

        if ( param ) {
            return vars[param] ? vars[param] : null;
        }
        return vars;
    }

    $scope.requestToken = function() {
        var credentialsObj = {
            appName: getParam('state'),
            username: null,
            password: getParam('code')
        }
        $http.post('/apps/credentials', credentialsObj).
            then(function successCallback(response) {
                $scope.result = "successful";
            }, function errorCallback(response) {
                $scope.result = "error";
            });
    }

    $scope.requestToken();
});