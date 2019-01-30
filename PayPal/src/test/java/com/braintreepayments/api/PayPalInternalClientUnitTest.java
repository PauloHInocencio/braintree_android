package com.braintreepayments.api;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.robolectric.RobolectricTestRunner;
import org.skyscreamer.jsonassert.JSONAssert;

import java.util.ArrayList;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class PayPalInternalClientUnitTest {

    private Context context;

    private ClientToken clientToken;
    private TokenizationKey tokenizationKey;

    private PayPalDataCollector payPalDataCollector;

    PayPalInternalClientCallback payPalInternalClientCallback;

    @Before
    public void beforeEach() {
        context = mock(Context.class);
        clientToken = mock(ClientToken.class);
        tokenizationKey = mock(TokenizationKey.class);

        payPalDataCollector = mock(PayPalDataCollector.class);
        payPalInternalClientCallback = mock(PayPalInternalClientCallback.class);
    }

    @Test
    public void sendRequest_withBillingAgreement_sendsAllParameters() throws JSONException {
        BraintreeClient braintreeClient = new MockBraintreeClientBuilder()
                .configuration(Configuration.fromJson(Fixtures.CONFIGURATION_WITH_LIVE_PAYPAL))
                .authorization(clientToken)
                .build();
        when(clientToken.getBearer()).thenReturn("client-token-bearer");

        PayPalInternalClient sut = new PayPalInternalClient(braintreeClient, "sample-scheme", payPalDataCollector);

        PostalAddress shippingAddressOverride = new PostalAddress()
                .recipientName("Brianna Tree")
                .streetAddress("123 Fake St.")
                .extendedAddress("Apt. v.0")
                .locality("Oakland")
                .region("CA")
                .postalCode("12345")
                .countryCodeAlpha2("US");

        PayPalRequest payPalRequest = new PayPalRequest()
                .billingAgreementDescription("Billing Agreement Description")
                .merchantAccountId("sample-merchant-account-id")
                .landingPageType("sample-landing-page-type")
                .displayName("sample-display-name")
                .localeCode("US")
                .shippingAddressRequired(true)
                .shippingAddressEditable(true)
                .offerCredit(true)
                .shippingAddressOverride(shippingAddressOverride);

        sut.sendRequest(context, payPalRequest, true, payPalInternalClientCallback);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(braintreeClient).sendPOST(eq("/v1/paypal_hermes/setup_billing_agreement"), captor.capture(), any(HttpResponseCallback.class));

        String result = captor.getValue();
        JSONObject actual = new JSONObject(result);

        JSONObject expected = new JSONObject()
                .put("authorization_fingerprint", "client-token-bearer")
                .put("return_url", "sample-scheme://onetouch/v1/success")
                .put("cancel_url", "sample-scheme://onetouch/v1/cancel")
                .put("offer_paypal_credit", true)
                .put("description", "Billing Agreement Description")
                .put("experience_profile", new JSONObject()
                        .put("no_shipping", false)
                        .put("landing_page_type", "sample-landing-page-type")
                        .put("brand_name", "sample-display-name")
                        .put("locale_code", "US")
                        .put("address_override", false))
                .put("shipping_address", new JSONObject()
                        .put("line1", "123 Fake St.")
                        .put("line2", "Apt. v.0")
                        .put("city", "Oakland")
                        .put("state", "CA")
                        .put("postal_code", "12345")
                        .put("country_code", "US")
                        .put("recipient_name", "Brianna Tree"))
                .put("merchant_account_id", "sample-merchant-account-id");

        JSONAssert.assertEquals(expected, actual, true);
    }

    @Test
    public void sendRequest_withOneTimePayment_sendsAllParameters() throws JSONException {
        BraintreeClient braintreeClient = new MockBraintreeClientBuilder()
                .configuration(Configuration.fromJson(Fixtures.CONFIGURATION_WITH_LIVE_PAYPAL))
                .authorization(clientToken)
                .build();
        when(clientToken.getBearer()).thenReturn("client-token-bearer");

        PayPalInternalClient sut = new PayPalInternalClient(braintreeClient, "sample-scheme", payPalDataCollector);

        PostalAddress shippingAddressOverride = new PostalAddress()
                .recipientName("Brianna Tree")
                .streetAddress("123 Fake St.")
                .extendedAddress("Apt. v.0")
                .locality("Oakland")
                .region("CA")
                .postalCode("12345")
                .countryCodeAlpha2("US");

        PayPalLineItem item = new PayPalLineItem(PayPalLineItem.KIND_DEBIT, "Item 0", "1", "2");
        item.setDescription("A new item");
        item.setProductCode("abc-123");
        item.setUnitTaxAmount("1.50");
        item.setUrl("http://example.com");

        PayPalRequest payPalRequest = new PayPalRequest()
                .amount("1.00")
                .currencyCode("USD")
                .intent("authorize")
                .merchantAccountId("sample-merchant-account-id")
                .landingPageType("sample-landing-page-type")
                .displayName("sample-display-name")
                .localeCode("US")
                .shippingAddressRequired(true)
                .shippingAddressEditable(true)
                .offerCredit(true)
                .lineItems(Collections.singletonList(item))
                .shippingAddressOverride(shippingAddressOverride);

        sut.sendRequest(context, payPalRequest, false, payPalInternalClientCallback);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(braintreeClient).sendPOST(eq("/v1/paypal_hermes/create_payment_resource"), captor.capture(), any(HttpResponseCallback.class));

        String result = captor.getValue();
        JSONObject actual = new JSONObject(result);

        JSONObject expected = new JSONObject()
                .put("amount", "1.00")
                .put("currency_iso_code", "USD")
                .put("intent", "authorize")
                .put("authorization_fingerprint", "client-token-bearer")
                .put("return_url", "sample-scheme://onetouch/v1/success")
                .put("cancel_url", "sample-scheme://onetouch/v1/cancel")
                .put("offer_paypal_credit", true)
                .put("line_items", new JSONArray()
                        .put(new JSONObject()
                                .put("kind", "debit")
                                .put("name", "Item 0")
                                .put("quantity", "1")
                                .put("unit_amount", "2")
                                .put("description", "A new item")
                                .put("product_code", "abc-123")
                                .put("unit_tax_amount", "1.50")
                                .put("url", "http://example.com")))
                .put("experience_profile", new JSONObject()
                        .put("no_shipping", false)
                        .put("landing_page_type", "sample-landing-page-type")
                        .put("brand_name", "sample-display-name")
                        .put("locale_code", "US")
                        .put("address_override", false))
                .put("line1", "123 Fake St.")
                .put("line2", "Apt. v.0")
                .put("city", "Oakland")
                .put("state", "CA")
                .put("postal_code", "12345")
                .put("country_code", "US")
                .put("recipient_name", "Brianna Tree")
                .put("merchant_account_id", "sample-merchant-account-id");

        JSONAssert.assertEquals(expected, actual, true);
    }

    @Test
    public void sendRequest_withTokenizationKey_sendsClientKeyParam() throws JSONException {
        BraintreeClient braintreeClient = new MockBraintreeClientBuilder()
                .configuration(Configuration.fromJson(Fixtures.CONFIGURATION_WITH_LIVE_PAYPAL))
                .authorization(tokenizationKey)
                .build();
        when(tokenizationKey.getBearer()).thenReturn("tokenization-key-bearer");

        PayPalInternalClient sut = new PayPalInternalClient(braintreeClient, "sample-scheme", payPalDataCollector);

        PayPalRequest payPalRequest = new PayPalRequest();
        sut.sendRequest(context, payPalRequest, true, payPalInternalClientCallback);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(braintreeClient).sendPOST(anyString(), captor.capture(), any(HttpResponseCallback.class));

        String result = captor.getValue();
        JSONObject actual = new JSONObject(result);

        assertFalse(actual.has("authorization_fingerprint"));
        assertEquals("tokenization-key-bearer", actual.get("client_key"));
    }

    @Test
    public void sendRequest_withEmptyDisplayName_fallsBackToPayPalConfigurationDisplayName() throws JSONException {
        BraintreeClient braintreeClient = new MockBraintreeClientBuilder()
                .configuration(Configuration.fromJson(Fixtures.CONFIGURATION_WITH_LIVE_PAYPAL))
                .authorization(tokenizationKey)
                .build();

        PayPalInternalClient sut = new PayPalInternalClient(braintreeClient, "sample-scheme", payPalDataCollector);

        PayPalRequest payPalRequest = new PayPalRequest().displayName("");
        sut.sendRequest(context, payPalRequest, true, payPalInternalClientCallback);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(braintreeClient).sendPOST(anyString(), captor.capture(), any(HttpResponseCallback.class));

        String result = captor.getValue();
        JSONObject actual = new JSONObject(result);

        assertEquals("paypal_merchant", ((JSONObject) actual.get("experience_profile")).get("brand_name"));
    }

    @Test
    public void sendRequest_withLocaleNotSpecified_omitsLocale() throws JSONException {
        BraintreeClient braintreeClient = new MockBraintreeClientBuilder()
                .configuration(Configuration.fromJson(Fixtures.CONFIGURATION_WITH_LIVE_PAYPAL))
                .authorization(tokenizationKey)
                .build();

        PayPalInternalClient sut = new PayPalInternalClient(braintreeClient, "sample-scheme", payPalDataCollector);

        PayPalRequest payPalRequest = new PayPalRequest().localeCode(null);
        sut.sendRequest(context, payPalRequest, true, payPalInternalClientCallback);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(braintreeClient).sendPOST(anyString(), captor.capture(), any(HttpResponseCallback.class));

        String result = captor.getValue();
        JSONObject actual = new JSONObject(result);

        assertFalse(((JSONObject) actual.get("experience_profile")).has("locale_code"));
    }

    @Test
    public void sendRequest_withMerchantAccountIdNotSpecified_omitsMerchantAccountId() throws JSONException {
        BraintreeClient braintreeClient = new MockBraintreeClientBuilder()
                .configuration(Configuration.fromJson(Fixtures.CONFIGURATION_WITH_LIVE_PAYPAL))
                .authorization(tokenizationKey)
                .build();

        PayPalInternalClient sut = new PayPalInternalClient(braintreeClient, "sample-scheme", payPalDataCollector);

        PayPalRequest payPalRequest = new PayPalRequest().merchantAccountId(null);
        sut.sendRequest(context, payPalRequest, true, payPalInternalClientCallback);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(braintreeClient).sendPOST(anyString(), captor.capture(), any(HttpResponseCallback.class));

        String result = captor.getValue();
        JSONObject actual = new JSONObject(result);

        assertFalse(actual.has("merchant_account_id"));
    }

    @Test
    public void sendRequest_withShippingAddressOverrideNotSpecified_sendsAddressOverrideFalse() throws JSONException {
        BraintreeClient braintreeClient = new MockBraintreeClientBuilder()
                .configuration(Configuration.fromJson(Fixtures.CONFIGURATION_WITH_LIVE_PAYPAL))
                .authorization(tokenizationKey)
                .build();

        PayPalInternalClient sut = new PayPalInternalClient(braintreeClient, "sample-scheme", payPalDataCollector);

        PayPalRequest payPalRequest = new PayPalRequest().shippingAddressOverride(null);
        sut.sendRequest(context, payPalRequest, true, payPalInternalClientCallback);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(braintreeClient).sendPOST(anyString(), captor.capture(), any(HttpResponseCallback.class));

        String result = captor.getValue();
        JSONObject actual = new JSONObject(result);

        assertEquals(false, ((JSONObject) actual.get("experience_profile")).get("address_override"));
    }

    @Test
    public void sendRequest_withShippingAddressSpecified_sendsAddressOverrideBasedOnShippingAdressEditability() throws JSONException {
        BraintreeClient braintreeClient = new MockBraintreeClientBuilder()
                .configuration(Configuration.fromJson(Fixtures.CONFIGURATION_WITH_LIVE_PAYPAL))
                .authorization(clientToken)
                .build();
        when(clientToken.getBearer()).thenReturn("client-token-bearer");

        PayPalInternalClient sut = new PayPalInternalClient(braintreeClient, "sample-scheme", payPalDataCollector);

        PayPalRequest payPalRequest = new PayPalRequest()
                .shippingAddressEditable(false)
                .shippingAddressOverride(new PostalAddress());

        sut.sendRequest(context, payPalRequest, true, payPalInternalClientCallback);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(braintreeClient).sendPOST(eq("/v1/paypal_hermes/setup_billing_agreement"), captor.capture(), any(HttpResponseCallback.class));

        String result = captor.getValue();
        JSONObject actual = new JSONObject(result);

        assertEquals(true, ((JSONObject) actual.get("experience_profile")).get("address_override"));
    }

    @Test
    public void sendRequest_withBillingAgreement_omitsEmptyBillingAgreementDescription() throws JSONException {
        BraintreeClient braintreeClient = new MockBraintreeClientBuilder()
                .configuration(Configuration.fromJson(Fixtures.CONFIGURATION_WITH_LIVE_PAYPAL))
                .authorization(tokenizationKey)
                .build();

        PayPalInternalClient sut = new PayPalInternalClient(braintreeClient, "sample-scheme", payPalDataCollector);

        PayPalRequest payPalRequest = new PayPalRequest().billingAgreementDescription("");
        sut.sendRequest(context, payPalRequest, true, payPalInternalClientCallback);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(braintreeClient).sendPOST(anyString(), captor.capture(), any(HttpResponseCallback.class));

        String result = captor.getValue();
        JSONObject actual = new JSONObject(result);

        assertFalse(actual.has("description"));
    }

    @Test
    public void sendRequest_withOneTimePayment_fallsBackToPayPalConfigurationCurrencyCode() throws JSONException {
        BraintreeClient braintreeClient = new MockBraintreeClientBuilder()
                .configuration(Configuration.fromJson(Fixtures.CONFIGURATION_WITH_LIVE_PAYPAL_INR))
                .authorization(tokenizationKey)
                .build();

        PayPalInternalClient sut = new PayPalInternalClient(braintreeClient, "sample-scheme", payPalDataCollector);

        PayPalRequest payPalRequest = new PayPalRequest();
        sut.sendRequest(context, payPalRequest, false, payPalInternalClientCallback);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(braintreeClient).sendPOST(anyString(), captor.capture(), any(HttpResponseCallback.class));

        String result = captor.getValue();
        JSONObject actual = new JSONObject(result);

        assertEquals("INR", actual.get("currency_iso_code"));
    }

    @Test
    public void sendRequest_withOneTimePayment_omitsEmptyLineItems() throws JSONException {
        BraintreeClient braintreeClient = new MockBraintreeClientBuilder()
                .configuration(Configuration.fromJson(Fixtures.CONFIGURATION_WITH_LIVE_PAYPAL))
                .authorization(tokenizationKey)
                .build();

        PayPalInternalClient sut = new PayPalInternalClient(braintreeClient, "sample-scheme", payPalDataCollector);

        PayPalRequest payPalRequest = new PayPalRequest().lineItems(new ArrayList<PayPalLineItem>());
        sut.sendRequest(context, payPalRequest, false, payPalInternalClientCallback);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(braintreeClient).sendPOST(anyString(), captor.capture(), any(HttpResponseCallback.class));

        String result = captor.getValue();
        JSONObject actual = new JSONObject(result);

        assertFalse(actual.has("line_items"));
    }

    @Test
    public void sendRequest_withBillingAgreement_callsBackPayPalResponseOnSuccess() throws JSONException {
        when(payPalDataCollector.getClientMetadataId(context)).thenReturn("sample-client-metadata-id");

        BraintreeClient braintreeClient = new MockBraintreeClientBuilder()
                .configuration(Configuration.fromJson(Fixtures.CONFIGURATION_WITH_LIVE_PAYPAL))
                .authorization(clientToken)
                .sendPOSTSuccessfulResponse(Fixtures.PAYPAL_HERMES_BILLING_AGREEMENT_RESPONSE)
                .build();

        PayPalInternalClient sut = new PayPalInternalClient(braintreeClient, "sample-scheme", payPalDataCollector);

        PayPalRequest payPalRequest = new PayPalRequest()
                .intent("authorize")
                .merchantAccountId("sample-merchant-account-id")
                .userAction(PayPalRequest.USER_ACTION_COMMIT);

        sut.sendRequest(context, payPalRequest, true, payPalInternalClientCallback);

        ArgumentCaptor<PayPalResponse> captor = ArgumentCaptor.forClass(PayPalResponse.class);
        verify(payPalInternalClientCallback).onResult(captor.capture(), (Exception) isNull());

        String expectedUrl =
            "https://checkout.paypal.com/one-touch-login-sandbox/index.html?action=create_payment_resource\u0026authorization_fingerprint=63cc461306c35080ce674a3372bffe1580b4130c7fd33d33968aa76bb93cdd06%7Ccreated_at%3D2015-10-13T18%3A49%3A48.371382792%2B0000%26merchant_id%3Ddcpspy2brwdjr3qn%26public_key%3D9wwrzqk3vr3t4nc8\u0026cancel_url=com.braintreepayments.api.test.braintree%3A%2F%2Fonetouch%2Fv1%2Fcancel\u0026controller=client_api%2Fpaypal_hermes\u0026experience_profile%5Baddress_override%5D=false\u0026experience_profile%5Bno_shipping%5D=false\u0026merchant_id=dcpspy2brwdjr3qn\u0026return_url=com.braintreepayments.api.test.braintree%3A%2F%2Fonetouch%2Fv1%2Fsuccess\u0026ba_token=EC-HERMES-SANDBOX-EC-TOKEN\u0026offer_paypal_credit=true\u0026version=1\u0026useraction=commit";

        PayPalResponse payPalResponse = captor.getValue();
        assertTrue(payPalResponse.isBillingAgreement());
        assertEquals("authorize", payPalResponse.getIntent());
        assertEquals("sample-merchant-account-id", payPalResponse.getMerchantAccountId());
        assertEquals("sample-scheme://onetouch/v1/success", payPalResponse.getSuccessUrl());
        assertEquals("EC-HERMES-SANDBOX-EC-TOKEN", payPalResponse.getPairingId());
        assertEquals("sample-client-metadata-id", payPalResponse.getClientMetadataId());
        assertEquals(expectedUrl, payPalResponse.getApprovalUrl());
    }

    @Test
    public void sendRequest_withOneTimePayment_callsBackPayPalResponseOnSuccess() throws JSONException {
        when(payPalDataCollector.getClientMetadataId(context)).thenReturn("sample-client-metadata-id");

        BraintreeClient braintreeClient = new MockBraintreeClientBuilder()
                .configuration(Configuration.fromJson(Fixtures.CONFIGURATION_WITH_LIVE_PAYPAL))
                .authorization(clientToken)
                .sendPOSTSuccessfulResponse(Fixtures.PAYPAL_HERMES_RESPONSE)
                .build();

        PayPalInternalClient sut = new PayPalInternalClient(braintreeClient, "sample-scheme", payPalDataCollector);

        PayPalRequest payPalRequest = new PayPalRequest()
                .intent("authorize")
                .merchantAccountId("sample-merchant-account-id")
                .userAction(PayPalRequest.USER_ACTION_COMMIT);

        sut.sendRequest(context, payPalRequest, false, payPalInternalClientCallback);

        ArgumentCaptor<PayPalResponse> captor = ArgumentCaptor.forClass(PayPalResponse.class);
        verify(payPalInternalClientCallback).onResult(captor.capture(), (Exception) isNull());

        String expectedUrl =
                "https://checkout.paypal.com/one-touch-login-sandbox/index.html?action=create_payment_resource\u0026amount=1.00\u0026authorization_fingerprint=63cc461306c35080ce674a3372bffe1580b4130c7fd33d33968aa76bb93cdd06%7Ccreated_at%3D2015-10-13T18%3A49%3A48.371382792%2B0000%26merchant_id%3Ddcpspy2brwdjr3qn%26public_key%3D9wwrzqk3vr3t4nc8\u0026cancel_url=com.braintreepayments.api.test.braintree%3A%2F%2Fonetouch%2Fv1%2Fcancel\u0026controller=client_api%2Fpaypal_hermes\u0026currency_iso_code=USD\u0026experience_profile%5Baddress_override%5D=false\u0026experience_profile%5Bno_shipping%5D=false\u0026merchant_id=dcpspy2brwdjr3qn\u0026return_url=com.braintreepayments.api.test.braintree%3A%2F%2Fonetouch%2Fv1%2Fsuccess\u0026token=EC-HERMES-SANDBOX-EC-TOKEN\u0026offer_paypal_credit=true\u0026version=1\u0026useraction=commit";

        PayPalResponse payPalResponse = captor.getValue();
        assertFalse(payPalResponse.isBillingAgreement());
        assertEquals("authorize", payPalResponse.getIntent());
        assertEquals("sample-merchant-account-id", payPalResponse.getMerchantAccountId());
        assertEquals("sample-scheme://onetouch/v1/success", payPalResponse.getSuccessUrl());
        assertEquals("EC-HERMES-SANDBOX-EC-TOKEN", payPalResponse.getPairingId());
        assertEquals("sample-client-metadata-id", payPalResponse.getClientMetadataId());
        assertEquals(expectedUrl, payPalResponse.getApprovalUrl());
    }

    @Test
    public void sendRequest_setsApprovalUrlUserActionToEmptyStringOnDefault() throws JSONException {
        BraintreeClient braintreeClient = new MockBraintreeClientBuilder()
                .configuration(Configuration.fromJson(Fixtures.CONFIGURATION_WITH_LIVE_PAYPAL))
                .authorization(clientToken)
                .sendPOSTSuccessfulResponse(Fixtures.PAYPAL_HERMES_RESPONSE)
                .build();

        PayPalInternalClient sut = new PayPalInternalClient(braintreeClient, "sample-scheme", payPalDataCollector);

        PayPalRequest payPalRequest = new PayPalRequest()
                .intent("authorize")
                .merchantAccountId("sample-merchant-account-id");

        sut.sendRequest(context, payPalRequest, false, payPalInternalClientCallback);

        ArgumentCaptor<PayPalResponse> captor = ArgumentCaptor.forClass(PayPalResponse.class);
        verify(payPalInternalClientCallback).onResult(captor.capture(), (Exception) isNull());

        String expectedUrl =
                "https://checkout.paypal.com/one-touch-login-sandbox/index.html?action=create_payment_resource\u0026amount=1.00\u0026authorization_fingerprint=63cc461306c35080ce674a3372bffe1580b4130c7fd33d33968aa76bb93cdd06%7Ccreated_at%3D2015-10-13T18%3A49%3A48.371382792%2B0000%26merchant_id%3Ddcpspy2brwdjr3qn%26public_key%3D9wwrzqk3vr3t4nc8\u0026cancel_url=com.braintreepayments.api.test.braintree%3A%2F%2Fonetouch%2Fv1%2Fcancel\u0026controller=client_api%2Fpaypal_hermes\u0026currency_iso_code=USD\u0026experience_profile%5Baddress_override%5D=false\u0026experience_profile%5Bno_shipping%5D=false\u0026merchant_id=dcpspy2brwdjr3qn\u0026return_url=com.braintreepayments.api.test.braintree%3A%2F%2Fonetouch%2Fv1%2Fsuccess\u0026token=EC-HERMES-SANDBOX-EC-TOKEN\u0026offer_paypal_credit=true\u0026version=1\u0026useraction=";

        PayPalResponse payPalResponse = captor.getValue();
        assertEquals(expectedUrl, payPalResponse.getApprovalUrl());
    }

    @Test
    public void sendRequest_propagatesHttpErrors() throws JSONException {
        Exception httpError = new Exception("http error");
        BraintreeClient braintreeClient = new MockBraintreeClientBuilder()
                .configuration(Configuration.fromJson(Fixtures.CONFIGURATION_WITH_LIVE_PAYPAL))
                .authorization(clientToken)
                .sendPOSTErrorResponse(httpError)
                .build();

        PayPalInternalClient sut = new PayPalInternalClient(braintreeClient, "sample-scheme", payPalDataCollector);

        PayPalRequest payPalRequest = new PayPalRequest();
        sut.sendRequest(context, payPalRequest, false, payPalInternalClientCallback);

        verify(payPalInternalClientCallback).onResult(null, httpError);
    }

    @Test
    public void sendRequest_propagatesMalformedJSONResponseErrors() throws JSONException {
        BraintreeClient braintreeClient = new MockBraintreeClientBuilder()
                .configuration(Configuration.fromJson(Fixtures.CONFIGURATION_WITH_LIVE_PAYPAL))
                .authorization(clientToken)
                .sendPOSTSuccessfulResponse("{bad:")
                .build();

        PayPalInternalClient sut = new PayPalInternalClient(braintreeClient, "sample-scheme", payPalDataCollector);

        PayPalRequest payPalRequest = new PayPalRequest();
        sut.sendRequest(context, payPalRequest, false, payPalInternalClientCallback);

        verify(payPalInternalClientCallback).onResult((PayPalResponse) isNull(), any(JSONException.class));
    }

    @Test
    public void sendRequest_propagatesGetConfigurationErrors() {
        Exception configurationError = new Exception("configuration error");
        BraintreeClient braintreeClient = new MockBraintreeClientBuilder()
                .authorization(clientToken)
                .configurationError(configurationError)
                .build();

        PayPalInternalClient sut = new PayPalInternalClient(braintreeClient, "sample-scheme", payPalDataCollector);

        PayPalRequest payPalRequest = new PayPalRequest();
        sut.sendRequest(context, payPalRequest, false, payPalInternalClientCallback);

        verify(payPalInternalClientCallback).onResult(null, configurationError);
    }
}