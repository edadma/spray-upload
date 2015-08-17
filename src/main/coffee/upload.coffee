app = angular.module( 'upload', ['angularFileUpload'] )

app.controller( 'uploadFormCtrl', ['$scope', 'FileUploader', ($scope, FileUploader) ->
	
	$scope.uploaded = false
	
	$scope.uploader = new FileUploader(
		url: 'ng-upload'
			
		onAfterAddingFile: (item) ->
			$scope.uploaded = false
			
			if this.queue.length > 1
				this.queue.shift()

		onSuccessItem: (item, response, status, headers) ->
			$scope.uploaded = true
		)
		
	] )