package com.gamfig.monitorabrasil.model;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.util.Log;
import android.widget.ImageView;

import com.gamfig.monitorabrasil.application.AppController;
import com.parse.ParseObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class Imagens {

    public static void getFotoPolitico (ParseObject politico, ImageView img){
		if(politico.getString("tipo").equals("c"))
			AppController.getInstance().getmImagemLoader().displayImage(AppController.URL_FOTO_DEPUTADO + politico.get("idCadastro") + ".jpg", img);
		else
			AppController.getInstance().getmImagemLoader().displayImage(AppController.URL_FOTO_SENADOR + politico.get("idCadastro") + ".jpg", img);

    }

    public static  void carregaImagemFacebook(String idFacebook,ImageView img,String tamanho){
        String url="http://graph.facebook.com/"+idFacebook+"/picture?type="+tamanho;
        AppController.getInstance().getmImagemLoader().displayImage(url,img);
    }

	public static Bitmap getImageBitmap(String id) {
		Bitmap bm = null;
		try {
			URL aURL = new URL(id);
			URLConnection conn = aURL.openConnection();
			conn.connect();
			InputStream is = conn.getInputStream();
			BufferedInputStream bis = new BufferedInputStream(is);
			bm = BitmapFactory.decodeStream(bis);
			bis.close();
			is.close();
		} catch (IOException e) {
			try {
				URL aURL = new URL("http://www.camara.gov.br/internet/deputado/bandep/" + id + ".jpg");
				URLConnection conn = aURL.openConnection();
				conn.connect();
				InputStream is = conn.getInputStream();
				BufferedInputStream bis = new BufferedInputStream(is);
				bm = BitmapFactory.decodeStream(bis);
				bis.close();
				is.close();
			} catch (IOException e1) {
				try {
					URL aURL = new URL("http://www.senado.gov.br/senadores/img/fotos/bemv" + id + ".jpg");
					URLConnection conn = aURL.openConnection();
					conn.connect();
					InputStream is = conn.getInputStream();
					BufferedInputStream bis = new BufferedInputStream(is);
					bm = BitmapFactory.decodeStream(bis);
					bis.close();
					is.close();
				} catch (IOException e2) {
					Log.e("", "Error getting bitmap", e);
				}

			}
		}
		return bm;
	}

	public static Bitmap getCroppedBitmap(Bitmap bitmap) {

		Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Config.ARGB_8888);
		Canvas canvas = new Canvas(output);

		final int color = 0xff424242;
		final Paint paint = new Paint();
		final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

		paint.setAntiAlias(true);
		canvas.drawARGB(0, 0, 0, 0);
		paint.setColor(color);
		// canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
		canvas.drawCircle(bitmap.getWidth() / 2, bitmap.getHeight() / 2, bitmap.getWidth() / 2, paint);
		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		canvas.drawBitmap(bitmap, rect, rect, paint);
		// Bitmap _bmp = Bitmap.createScaledBitmap(output, 60, 60, false);
		// return _bmp;
		return output;
	}
}
