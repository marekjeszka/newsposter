var app = angular.module('myapp', []);

app.controller('posterController', function($scope, $http, $window){
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
        $http.post('/wordpress', postObj);
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