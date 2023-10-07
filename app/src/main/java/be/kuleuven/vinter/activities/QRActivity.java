package be.kuleuven.vinter.activities;

import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.graphics.pdf.PdfRenderer;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileUtils;
import android.os.ParcelFileDescriptor;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import be.kuleuven.vinter.R;

public class QRActivity extends AppCompatActivity {
    private RequestQueue requestQueue;

    private Bitmap myScaledQR;

    private int productId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr);

        requestQueue = Volley.newRequestQueue(this);

        productId = getIntent().getExtras().getInt("productId");

        String TextQR = "https://studev.groept.be/api/a21pt311/addRegisteredProduct/" + productId;
        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
        try {
            BitMatrix bitMatrix = multiFormatWriter.encode(TextQR, BarcodeFormat.QR_CODE, 2000, 2000);
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap myQR = barcodeEncoder.createBitmap(bitMatrix);
            myScaledQR = Bitmap.createScaledBitmap(myQR,1000, 1000,false);
        }
        catch (Exception e){
            e.printStackTrace();
        }

        ActivityCompat.requestPermissions(QRActivity.this, new String[]{
                WRITE_EXTERNAL_STORAGE}, PackageManager.PERMISSION_GRANTED); //permission om te downloaden in storage

        getOrderData(); //pdf maken en laten zien
    }

    private void pdfToBitmap(File pdfFile) {

        try {
            ImageView pdfViewer = findViewById(R.id.pdfViewer);
            int width = pdfViewer.getWidth();
            int height = pdfViewer.getHeight();

            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_4444);
            //File file = new File("....");
            PdfRenderer renderer = new PdfRenderer(ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY));
            int currentPage = 0;
            System.out.println(renderer.getPageCount());
            Matrix m = pdfViewer.getImageMatrix();
            Rect rect = new Rect(0,0, width, height);
            renderer.openPage(currentPage).render(bitmap,rect,m,PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
            pdfViewer.setImageMatrix(m);
            pdfViewer.setImageBitmap(bitmap);
            pdfViewer.invalidate();
            }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void getOrderData()
    {
        final String GET_ORDER_DATA_URL = "https://studev.groept.be/api/a21pt311/getOrderData/"+ productId;
        JsonArrayRequest retrieveImageRequest = new JsonArrayRequest(Request.Method.GET, GET_ORDER_DATA_URL, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        JSONObject o = null;
                        try {
                            o = response.getJSONObject(0);
                            generatePDF(o.getString("street"),o.getString("streetNb"), o.getString("city"),
                                    o.getString("postalCode"), o.getString("email"));

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                    }
                }
        );

        requestQueue.add(retrieveImageRequest);
    }

    private void generatePDF(String street, String streetNb, String city, String postalCode, String email) {
        PdfDocument pdfDocument = new PdfDocument();
        Paint myPaint = new Paint();
        Paint title = new Paint();

        int pdfHeight = 3508;
        int pdfWidth = 2480;
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pdfWidth, pdfHeight, 1).create();
        PdfDocument.Page myPage = pdfDocument.startPage(pageInfo);
        Canvas canvas = myPage.getCanvas();

        //write and draw on page
        //...

        canvas.drawBitmap(myScaledQR,50,100,myPaint);

        title.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
        //title.setColor(ContextCompat.getColor(QRActivity.this, R.color.purple_200));
        title.setColor(Color.BLACK);
        title.setTextSize(50);
        title.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("Place this sticker on the box!", 550, 1100, title);

        title.setTextSize(50);
        canvas.drawText("Buyer: ", 550, 1200, title);
        canvas.drawText(email, 550, 1300, title);

        canvas.drawText("To address: " , 550, 1400, title);
        canvas.drawText(street + " " + streetNb , 550, 1500, title);
        canvas.drawText(postalCode + " " + city, 550, 1600, title);



        //...
        //write and draw on page

        pdfDocument.finishPage(myPage);

        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                "/StickerQR"+productId+".pdf");
        try {
            pdfDocument.writeTo(new FileOutputStream(file));

        }
        catch (IOException e) {
            e.printStackTrace();
        }
        pdfDocument.close();
        Toast.makeText(QRActivity.this, "PDF generated and downloaded!", Toast.LENGTH_SHORT).show();
        pdfToBitmap(file);
    }

    public void goBack(View view) {
        finish();
    }

}