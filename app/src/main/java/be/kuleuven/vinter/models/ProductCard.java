package be.kuleuven.vinter.models;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import be.kuleuven.vinter.R;

public class ProductCard implements Parcelable {
    private int productId;
    private String name;
    private int price;
    private ArrayList<Bitmap> images;
    private int imageIndex;
    private Boolean isLiked;

    private String brand;
    private int size;
    private String gender;
    private String color;
    private String category;
    private int sellerID;


    private LocalDateTime dateTimeOfLoadingIn;

    public ProductCard(int id, String name, int price)
    {
        this.productId = id;
        this.name = name;
        this.price = price;
        this.isLiked = null;
        images = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //ZonedDateTime zdt = ZonedDateTime.of(LocalDateTime.now(), ZoneOffset.UTC); // Maak subclass //TODO
            //ZoneId zId = ZoneId.of("CET");
            dateTimeOfLoadingIn = LocalDateTime.now(); //LocalDateTime.ofInstant(zdt.toInstant(), zId);
            //dateTimeOfLoadingIn = LocalDateTime.now();
        }
    }

    protected ProductCard(Parcel in) {
        productId = in.readInt();
        name = in.readString();
        price = in.readInt();
        //images = in.readArrayList(null);
        images = new ArrayList<>();
        ArrayList<byte[]> compressedImages = in.readArrayList(null);
        for (byte[] bytes: compressedImages){
            Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            images.add(bmp);
        }
        imageIndex = in.readInt();

        brand = in.readString();
        size = in.readInt();
        gender = in.readString();
        color = in.readString();
        category = in.readString();
        sellerID = in.readInt();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            dateTimeOfLoadingIn = (LocalDateTime) in.readSerializable();
        }

    }

    public ProductCard(JSONObject o) {
        try {
            productId = o.getInt("idProduct");
            name =  o.getString("model").replaceAll("_", " ");;
            price = o.getInt("price");

            brand = o.getString("brand");
            size = o.getInt("size");
            gender = o.getString("gender");
            color = o.getString("color");
            category =  o.getString("category");
            sellerID = o.getInt("sellerID");
            images = new ArrayList<>();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                dateTimeOfLoadingIn = LocalDateTime.now();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static final Creator<ProductCard> CREATOR = new Creator<ProductCard>() {
        @Override
        public ProductCard createFromParcel(Parcel in) {
            return new ProductCard(in);
        }

        @Override
        public ProductCard[] newArray(int size) {
            return new ProductCard[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(productId);
        parcel.writeString(name);
        parcel.writeInt(price);
        List<byte[]> compressedImages = new ArrayList<>();

        for (Bitmap image: images){
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            image.compress(Bitmap.CompressFormat.JPEG, 90, stream); // Change quality bij tooLargeException
            byte[] bytes = stream.toByteArray();
            compressedImages.add(bytes);
        }
        parcel.writeList(compressedImages); // Had to be compressed; otherwise "TransactionTooLargeException"
        parcel.writeInt(imageIndex);
        //parcel.writeByte((byte) (isLiked == null ? 0 : isLiked ? 1 : 2));

        parcel.writeString(brand);
        parcel.writeInt(size);
        parcel.writeString(gender);
        parcel.writeString(color);
        parcel.writeString(category);
        parcel.writeInt(sellerID);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            parcel.writeSerializable(dateTimeOfLoadingIn);
        }
    }


    public void incrementImageNb(){
        if (imageIndex < images.size()-1){
            imageIndex++;
        }
        else {
            imageIndex = 0;
        }
    }

    public int getProductId() {
        return productId;
    }

    public String getName() {
        return name;
    }

    public int getPrice() {
        return price;
    }

    public ArrayList<Bitmap> getImages() {
        return images;
    }

    public Bitmap getSpecImage() {
        //Bitmap bitMapIcon = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
        //if (images.size() == 0) { return bitMapIcon ;}
        return images.get(imageIndex);
    }

    public String getBrand() {
        return brand;
    }

    public int getSize() {
        return size;
    }

    public String getGender() {
        return gender;
    }

    public String getColor() {
        return color;
    }

    public String getCategory() {
        return category;
    }

    public int getSellerID() {
        return sellerID;
    }

    public LocalDateTime getDateTimeOfLoadingIn() {
        return dateTimeOfLoadingIn;
    }

    public void setDateTimeOfLoadingIn(LocalDateTime dateTimeOfLoadingIn) {
        this.dateTimeOfLoadingIn = dateTimeOfLoadingIn;
    }

    public void addImage(Bitmap newImage) {
        images.add(newImage);
    }
    public void clearImages() {
        images.clear();
    }
    public void setProductId(int id) {
        this.productId = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public void like(){ isLiked = true; }

    public void dislike(){ isLiked = false; }

    public Boolean getIsLiked() {
        return isLiked;
    }

    public int getImageIndex() {
        return imageIndex;
    }

    @Override
    public String toString() {
        return "ProductCard{" +
                "userId=" + productId +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", imageIndex=" + imageIndex +
                ", isLiked=" + isLiked +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductCard that = (ProductCard) o;
        return productId == that.productId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(productId);
    }
}
