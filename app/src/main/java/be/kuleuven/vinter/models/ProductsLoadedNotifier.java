package be.kuleuven.vinter.models;

public interface ProductsLoadedNotifier {

    void notifyProductsLoaded();

    void notifyNoProductsToBeLoaded();

    void notifyProductsListEmpty();

    void notifyProductsListNotEmptyAnymore();

}
