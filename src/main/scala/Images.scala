package xyz.hyperreal.upload

import spray.http._
import MediaTypes._

import java.io.{ByteArrayInputStream, ByteArrayOutputStream, File}
import java.nio.file.Files
import javax.imageio.ImageIO
import java.awt.image.BufferedImage
import java.awt.Image


object Images {

	def http( image: Array[Byte] ) = HttpResponse( entity = HttpEntity(ContentType(`image/jpeg`), HttpData(image)) )
	
	def avatar( image: Array[Byte], size: Int, thumb: Int ) = (toJPEG( image, Some(size, size) ), toJPEG( image, Some(thumb, thumb) ))
	
	def toJPEG( image: Array[Byte], resize: Option[(Int, Int)] ) = convert( image, "JPEG", resize )
	
	def convert( image: Array[Byte], typ: String, resize: Option[(Int, Int)] ) = {
		val img = ImageIO.read( new ByteArrayInputStream(image) )
		val scale = resize != None
		val (width, height) =
			resize match {
				case Some( (w, h) ) => (w, h)
				case None => (img.getWidth, img.getHeight)
			}
		val buf = new BufferedImage( width, height, BufferedImage.TYPE_INT_RGB )
		val data = new ByteArrayOutputStream
		
		buf.getGraphics.drawImage( if (scale) img.getScaledInstance(width, height, Image.SCALE_SMOOTH) else img, 0, 0, null )
		ImageIO.write( buf, typ, data )
		data.toByteArray
	}

}