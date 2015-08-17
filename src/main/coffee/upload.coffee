app = angular.module( 'upload', ['angularFileUpload'] )

app.controller( 'uploadFormCtrl', ['$scope', 'FileUploader', ($scope, FileUploader) ->
	
	$scope.uploader = new FileUploader(
		url: 'ng-upload'
		onAfterAddingFile: (item) ->
			if this.queue.length > 1
				this.queue.shift()
		)
		
	$scope.submit = ->
	
	] )