package xyz.hyperreal.upload

import akka.actor.ActorSystem

import spray.routing._
import spray.http._
import spray.json._
import spray.json.DefaultJsonProtocol._
import spray.httpx.SprayJsonSupport._
import MediaTypes._

import in.azeemarshad.common.sessionutils.SessionDirectives

import concurrent.duration._
import scala.util.{Success, Failure}


object Main extends App with SimpleRoutingApp with SessionDirectives {
	
	implicit val system = ActorSystem("on-spray-can")
	implicit val context = system.dispatcher

	startServer(interface = "localhost", port = 8080) {
		val errorHandler = ExceptionHandler {
			case e: RuntimeException => complete( StatusCodes.BadRequest, e.getMessage )
		}
		
		//
		// resource renaming routes (these will mostly be removed as soon as possible)
		//
		pathPrefix("sass") {
			getFromResourceDirectory("resources/public") } ~
		(pathPrefix("js") | pathPrefix("css")) {
			getFromResourceDirectory("public") } ~
		pathSuffixTest( """.*(?:\.(?:html|png|ico|jade))"""r ) { _ =>
			getFromResourceDirectory( "public" ) } ~
		pathPrefix("coffee") {
			getFromResourceDirectory("public/js") } ~
		pathPrefix("webjars") {
			getFromResourceDirectory("META-INF/resources/webjars") } ~
		//
		// application routes
		//
		(get & pathSingleSlash) {
			respondWithMediaType( `text/html` ) {
			complete( "<!DOCTYPE html>" +
				<html>
					<head>
						<meta charset="utf-8"/>
						<meta http-equiv="X-UA-Compatible" content="IE=edge"/>
						<meta name="viewport" content="width=device-width, initial-scale=1"/>
						
						<title>Upload</title>

						<link rel="shortcut icon" href="/favicon.ico"/>
						
						<link href="/webjars/bootstrap/3.3.5/css/bootstrap.min.css" rel="stylesheet"/>
						<link href="/webjars/bootstrap/3.3.5/css/bootstrap-theme.min.css" rel="stylesheet"/>
						<script src="/webjars/angularjs/1.4.3/angular.min.js"></script>
						<script src="/webjars/nervgh-angular-file-upload/1.1.5-1/angular-file-upload.js"></script>
						<script src="/coffee/upload.js"></script>
						<script src="/js/ngThumb.js"></script>
					</head>
					<body ng-app="upload">
					
						<form action="/upload" method="post" enctype="multipart/form-data">
							<fieldset>
								<legend>Traditional (non AngularJS based) Upload</legend>
								Select image to upload:
								<input type="file" name="file"/><br/>
								<input type="submit" name="submit"/>
							</fieldset>
						</form>

						<div ng-controller="uploadFormCtrl">
							<fieldset>
								<legend>AngularJS Based Upload</legend>
								Select image to upload:
								<input type="file" nv-file-select="" uploader="uploader"/><br/>
								<ul>
									<li ng-repeat="item in uploader.queue">
										<div ng-thumb="{ file: item._file, height: 100 }"></div>
										<button ng-click="item.upload()">upload</button>
									</li>
								</ul>
							</fieldset>
						</div>
						
						<a href="/download">download</a>
						
						<script src="/webjars/jquery/1.11.1/jquery.min.js"></script>
						<script src="/webjars/bootstrap/3.3.5/js/bootstrap.min.js"></script>
					</body>
				</html>
			)
		}} ~
		(post & path( "upload" ) & entity( as[MultipartFormData] )) { formData =>
			formData get "file" match {
				case None =>
					complete( StatusCodes.BadRequest, <h1>Missing file</h1> )
				case Some( b ) =>
					b.entity match {
						case e: HttpEntity.NonEmpty =>
							mime = e.asInstanceOf[HttpEntity.NonEmpty].contentType.mediaType
							image = e.data.toByteArray
							
							if (mime.isImage)
								redirect( "/download", StatusCodes.SeeOther )
							else
								complete( StatusCodes.BadRequest, <h1>Expected image file</h1> )
						case _ => complete( StatusCodes.BadRequest, <h1>Empty file</h1> )
					}
			}
		} ~
		(post & path( "ng-upload" ) & entity( as[MultipartFormData] )) { formData =>
			formData get "file" match {
				case None =>
					complete( StatusCodes.BadRequest, <h1>Missing file</h1> )
				case Some( b ) =>
					b.entity match {
						case e: HttpEntity.NonEmpty =>
							mime = e.asInstanceOf[HttpEntity.NonEmpty].contentType.mediaType
							image = e.data.toByteArray
							
							if (mime.isImage) {
								complete( "" )
							}
							else
								complete( StatusCodes.BadRequest, <h1>Expected image file</h1> )
						case _ => complete( StatusCodes.BadRequest, <h1>Empty file</h1> )
					}
			}
		} ~
		(get & path( "download" )) {
			complete( 
				<html>
					<head>
						<meta charset="utf-8"/>
						<meta http-equiv="X-UA-Compatible" content="IE=edge"/>
						<meta name="viewport" content="width=device-width, initial-scale=1"/>
						
						<title>image</title>

						<link rel="shortcut icon" href="/favicon.ico"/>
						
						<link href="/webjars/bootstrap/3.3.5/css/bootstrap.min.css" rel="stylesheet"/>
						<link href="/webjars/bootstrap/3.3.5/css/bootstrap-theme.min.css" rel="stylesheet"/>
					</head>
					<body>
						<img class="img-rounded" src="/download/200x200"/> <a href="/">upload</a>
					</body>
				</html>
			)
		} ~
		(get & path( "download"/IntNumber~"x"~IntNumber )) { (width, height) =>
			handleExceptions( errorHandler ) {
				complete( Images.http(Images.toJPEG(image, Some(width, height))) )
			}
		}
	}
	
	var mime: MediaType = null
	var image: Array[Byte] = null
}