var app = angular.module('myapp', []);

app.controller('posterController', function($scope, $http, $window){
    // TODO start using Spring filters for redirection
    $scope.init = function() {
        $http.get('/isAuthorized')
            .then(function successCallback(response) { },
                  function errorCallback(response) { $window.location.href = "/masterPassword.html"; });
    }

    $scope.sendPost = function() {
        var postObj = {
            topic: document.getElementById("postTopic").value,
            body: document.getElementById("postBody").value
        }
        $http.post('/post', postObj);
        // TODO provide some confirmation
    }

    $scope.init();
});

app.controller('loginController', function($scope, $http, $window) {
    $scope.init = function() {
        $http.get('/isAuthorized')
            .then(function successCallback(response) { $window.location.href = "/index.html"; },
                  function errorCallback(response) { } );
    }

    $scope.login = function() {
        var postObj = {
            password: document.getElementById("password").value,
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

    $scope.init();
});

app.controller('credentialsController', function($scope, $http, $window) {
    $scope.initApps = function() {
        $http.get('/apps').then(function successCallback(response) {
            $scope.apps = response.data;
        });
    }

    $scope.storeCredentials = function() {
        var credentialsObj = {
            appName: document.getElementById("appName").value,
            username: document.getElementById("username").value,
            password: document.getElementById("password").value
        }
        $http.post('/credentials', credentialsObj).then(function successCallback(response) {
            $scope.initApps();
        });
        // TODO close modal after adding
    }

    $scope.authorizeGmail = function() {
        $http.post('/authorize', document.getElementById("email").value)
             .then(function successCallback(response) {
                 $window.open(response.data, 'Google OAuth', 'width=640,height=540');
             });
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
        $http.post('/credentials', credentialsObj).
            then(function successCallback(response) {
                $window.close();
            });
    }

    $scope.requestToken();
});