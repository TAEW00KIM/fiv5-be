package com.teamfiv5.fiv5;

import com.google.cloud.firestore.Firestore;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessaging;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import software.amazon.awssdk.services.s3.S3Client;

@SpringBootTest
class Fiv5ApplicationTests {

    @MockBean
    private S3Client s3Client;

    @MockBean
    private Firestore firestore;

    @MockBean
    private FirebaseAuth firebaseAuth;
    @MockBean
    private FirebaseMessaging firebaseMessaging;

    @Test
    void contextLoads() {
    }

}
