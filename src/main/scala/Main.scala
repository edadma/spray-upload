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
		(get & path( "upload" )) {
			complete(
				<html>
				<body>
					<form action="/upload" method="post" enctype="multipart/form-data">
						Select image to upload:
						<input type="file" name="file"/><br/>
						<input type="submit" name="submit"/>
					</form>
				</body>
				</html>
			)
		} ~
		(post & path( "upload" ) & entity( as[MultipartFormData] )) { formData =>
			formData get "file" match {
				case None =>
					complete( StatusCodes.BadRequest, "Missing file" )
				case Some( b ) =>
					mime = b.entity.asInstanceOf[HttpEntity.NonEmpty].contentType.mediaType
					image = b.entity.data.toByteArray
					extension = mime.fileExtensions.head
					
					if (mime.isImage)
						redirect( "/download", StatusCodes.SeeOther )
					else
						complete( StatusCodes.BadRequest, "Expected image file" )
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
						<img class="img-rounded" src="/download/100/100"/> <a href="/upload">upload</a>
					</body>
				</html>
			)
		} ~
		(get & path( "download"/IntNumber/IntNumber )) { (width, height) =>
			complete( Images.http(Images.toJPEG(image, Some(width, height))) )
		}
	}
	
	var mime: MediaType = null
	var image: Array[Byte] = null
	var extension: String = null
}