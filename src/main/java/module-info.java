module com.example.zerbitzaratxata {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.zerbitzaratxata to javafx.fxml;
    exports com.example.zerbitzaratxata;
}