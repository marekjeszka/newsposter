var app = angular.module('myapp', []);

app.controller('posterController', function($scope, $http){
    $scope.sendPost = function() {
        var postObj = {
            topic: document.getElementById("postTopic").value,
            body: document.getElementById("postBody").value
        }
        $http.post('/wordpress', postObj);
    }
});