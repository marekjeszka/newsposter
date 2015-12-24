var app = angular.module('myapp', []);

app.controller('greetingCtrl', function($scope, $http){
    $scope.greetingResult = "Hello World!";
    waitingDialog.show('Greeting...');
    setTimeout(function(){
        waitingDialog.hide();
    }, 5000);
});