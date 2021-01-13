package com.braintreepayments.api;

import android.os.Parcel;

import com.braintreepayments.api.models.Configuration;
import com.braintreepayments.api.models.PayPalApiBillingAgreementRequest;
import com.braintreepayments.api.models.PayPalApiCheckoutRequest;
import com.braintreepayments.api.models.PayPalApiLineItem;
import com.braintreepayments.api.models.PayPalApiRequest;
import com.braintreepayments.api.models.PayPalRequest;
import com.braintreepayments.api.models.PostalAddress;
import com.braintreepayments.api.network.PayPalApiEnvironmentManager;
import com.braintreepayments.testutils.Fixtures;

import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.util.ArrayList;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
public class PayPalRequestUnitTest {

    private MockFragmentBuilder mMockFragmentBuilder;

    @Before
    public void setup() {
        mMockFragmentBuilder = new MockFragmentBuilder()
                .context(RuntimeEnvironment.application);
    }

    @Test
    public void getCheckoutRequest_containsCorrectValues() throws JSONException {
        Configuration configuration = Configuration.fromJson(Fixtures.CONFIGURATION_WITH_OFFLINE_PAYPAL);
        BraintreeFragment fragment = mMockFragmentBuilder.configuration(configuration).build();

        PayPalApiCheckoutRequest request = PayPal.getCheckoutRequest(fragment, "https://paypal.com/?token=pairingId");

        assertEquals(PayPalApiEnvironmentManager.MOCK, request.getEnvironment());
        assertEquals("com.braintreepayments.api.braintree://onetouch/v1/cancel", request.getCancelUrl());
        assertEquals("com.braintreepayments.api.braintree://onetouch/v1/success", request.getSuccessUrl());
        assertEquals("paypal_client_id", request.getClientId());
        assertEquals("pairingId", request.getPairingId());
    }

    @Test
    public void getCheckoutRequest_buildsWithLiveStageUrl() throws JSONException {
        Configuration configuration = Configuration.fromJson(Fixtures.CONFIGURATION_WITH_LIVE_PAYPAL);
        BraintreeFragment fragment = mMockFragmentBuilder.configuration(configuration).build();

        PayPalApiRequest request = PayPal.getCheckoutRequest(fragment, null);

        assertEquals(PayPalApiEnvironmentManager.LIVE, request.getEnvironment());
    }

    @Test
    public void getCheckoutRequest_buildsWithOfflineStageUrl() throws JSONException {
        Configuration configuration = Configuration.fromJson(Fixtures.CONFIGURATION_WITH_OFFLINE_PAYPAL);
        BraintreeFragment fragment = mMockFragmentBuilder.configuration(configuration).build();

        PayPalApiRequest request = PayPal.getCheckoutRequest(fragment, null);

        assertEquals(PayPalApiEnvironmentManager.MOCK, request.getEnvironment());
    }

    @Test
    public void getCheckoutRequest_buildsWithCustomStageUrl() throws JSONException {
        Configuration configuration = Configuration.fromJson(Fixtures.CONFIGURATION_WITH_CUSTOM_PAYPAL);
        BraintreeFragment fragment = mMockFragmentBuilder.configuration(configuration).build();

        PayPalApiRequest request = PayPal.getCheckoutRequest(fragment, null);

        assertEquals("custom", request.getEnvironment());
    }

    @Test
    public void getBillingAgreement_containsCorrectValues() throws JSONException {
        Configuration configuration = Configuration.fromJson(Fixtures.CONFIGURATION_WITH_OFFLINE_PAYPAL);
        BraintreeFragment fragment = mMockFragmentBuilder.configuration(configuration).build();

        PayPalApiBillingAgreementRequest request = PayPal.getBillingAgreementRequest(fragment,
                "https://paypal.com/?ba_token=pairingId");

        assertEquals(PayPalApiEnvironmentManager.MOCK, request.getEnvironment());
        assertEquals("com.braintreepayments.api.braintree://onetouch/v1/cancel", request.getCancelUrl());
        assertEquals("com.braintreepayments.api.braintree://onetouch/v1/success", request.getSuccessUrl());
        assertEquals("paypal_client_id", request.getClientId());
        assertEquals("pairingId", request.getPairingId());
    }

    @Test
    public void getBillingAgreementRequest_buildsWithLiveStageUrl() throws JSONException {
        Configuration configuration = Configuration.fromJson(Fixtures.CONFIGURATION_WITH_LIVE_PAYPAL);
        BraintreeFragment fragment = mMockFragmentBuilder.configuration(configuration).build();

        PayPalApiRequest request = PayPal.getBillingAgreementRequest(fragment, null);

        assertEquals(PayPalApiEnvironmentManager.LIVE, request.getEnvironment());
    }

    @Test
    public void getBillingAgreementRequest_buildsWithOfflineStageUrl() throws JSONException {
        Configuration configuration = Configuration.fromJson(Fixtures.CONFIGURATION_WITH_OFFLINE_PAYPAL);
        BraintreeFragment fragment = mMockFragmentBuilder.configuration(configuration).build();

        PayPalApiRequest request = PayPal.getBillingAgreementRequest(fragment, null);

        assertEquals(PayPalApiEnvironmentManager.MOCK, request.getEnvironment());
    }

    @Test
    public void getBillingAgreementRequest_buildsWithCustomStageUrl() throws JSONException {
        Configuration configuration = Configuration.fromJson(Fixtures.CONFIGURATION_WITH_CUSTOM_PAYPAL);
        BraintreeFragment fragment = mMockFragmentBuilder.configuration(configuration).build();

        PayPalApiRequest request = PayPal.getBillingAgreementRequest(fragment, null);

        assertEquals("custom", request.getEnvironment());
    }

    @Test
    public void parcelsCorrectly() {
        PayPalRequest request = new PayPalRequest("12.34")
                .currencyCode("USD")
                .localeCode("en-US")
                .billingAgreementDescription("Billing Agreement Description")
                .shippingAddressRequired(true)
                .shippingAddressEditable(true)
                .shippingAddressOverride(new PostalAddress()
                        .recipientName("Postal Address"))
                .intent(PayPalRequest.INTENT_SALE)
                .landingPageType(PayPalRequest.LANDING_PAGE_TYPE_LOGIN)
                .userAction(PayPalRequest.USER_ACTION_COMMIT)
                .displayName("Display Name")
                .offerCredit(true)
                .offerPayLater(true)
                .merchantAccountId("merchant_account_id");

        ArrayList<PayPalApiLineItem> lineItems = new ArrayList<PayPalApiLineItem>();
        lineItems.add(new PayPalApiLineItem(PayPalApiLineItem.KIND_DEBIT, "An Item", "1", "1"));
        request.lineItems(lineItems);

        Parcel parcel = Parcel.obtain();
        request.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);
        PayPalRequest result = PayPalRequest.CREATOR.createFromParcel(parcel);

        assertEquals("12.34", result.getAmount());
        assertEquals("USD", result.getCurrencyCode());
        assertEquals("en-US", result.getLocaleCode());
        assertEquals("Billing Agreement Description",
                result.getBillingAgreementDescription());
        assertEquals(true, result.isShippingAddressRequired());
        assertEquals(true, result.isShippingAddressEditable());
        assertEquals("Postal Address", result.getShippingAddressOverride()
                .getRecipientName());
        assertEquals(PayPalRequest.INTENT_SALE, result.getIntent());
        assertEquals(PayPalRequest.LANDING_PAGE_TYPE_LOGIN, result.getLandingPageType());
        assertEquals(PayPalRequest.USER_ACTION_COMMIT, result.getUserAction());
        assertEquals("Display Name", result.getDisplayName());
        assertTrue(result.shouldOfferCredit());
        assertTrue(result.shouldOfferPayLater());
        assertEquals("merchant_account_id", result.getMerchantAccountId());
        assertEquals(1, result.getLineItems().size());
        assertEquals("An Item", result.getLineItems().get(0).getName());
    }
}
