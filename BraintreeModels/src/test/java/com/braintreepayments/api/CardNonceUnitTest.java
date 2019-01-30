package com.braintreepayments.api;

import android.os.Parcel;

import org.json.JSONException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static com.braintreepayments.api.BinData.NO;
import static com.braintreepayments.api.BinData.UNKNOWN;
import static com.braintreepayments.api.BinData.YES;
import static com.braintreepayments.api.Assertions.assertBinDataEqual;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertNull;

@RunWith(RobolectricTestRunner.class)
public class CardNonceUnitTest {

    @Test
    public void canCreateCardFromJson() throws JSONException {
        CardNonce cardNonce = CardNonce.fromJson(Fixtures.PAYMENT_METHODS_RESPONSE_VISA_CREDIT_CARD);

        assertEquals("Visa", cardNonce.getTypeLabel());
        assertEquals("Visa", cardNonce.getCardType());
        assertEquals("123456-12345-12345-a-adfa", cardNonce.getNonce());
        assertEquals("ending in ••11", cardNonce.getDescription());
        assertEquals("11", cardNonce.getLastTwo());
        assertEquals("1111", cardNonce.getLastFour());
        assertNotNull(cardNonce.getThreeDSecureInfo());
        assertFalse(cardNonce.getThreeDSecureInfo().isLiabilityShifted());
        assertTrue(cardNonce.getThreeDSecureInfo().isLiabilityShiftPossible());
        assertNotNull(cardNonce.getBinData());
        assertEquals(UNKNOWN, cardNonce.getBinData().getPrepaid());
        assertEquals(YES, cardNonce.getBinData().getHealthcare());
        assertEquals(NO, cardNonce.getBinData().getDebit());
        assertEquals(UNKNOWN, cardNonce.getBinData().getDurbinRegulated());
        assertEquals(UNKNOWN, cardNonce.getBinData().getCommercial());
        assertEquals(UNKNOWN, cardNonce.getBinData().getPayroll());
        assertEquals(UNKNOWN, cardNonce.getBinData().getIssuingBank());
        assertEquals("Something", cardNonce.getBinData().getCountryOfIssuance());
        assertEquals("123", cardNonce.getBinData().getProductId());
        assertEquals("unregulated",
                cardNonce.getAuthenticationInsight().getRegulationEnvironment());
        assertEquals("01", cardNonce.getExpirationMonth());
        assertEquals("2020", cardNonce.getExpirationYear());
        assertEquals("Joe Smith", cardNonce.getCardholderName());
    }

    @Test
    public void canCreateCardFromTokenizeCreditCardGraphQLResponse() throws JSONException {
        CardNonce cardNonce = CardNonce.fromJson(Fixtures.GRAPHQL_RESPONSE_CREDIT_CARD);

        assertEquals("Visa", cardNonce.getTypeLabel());
        assertEquals("Visa", cardNonce.getCardType());
        assertEquals("3744a73e-b1ab-0dbd-85f0-c12a0a4bd3d1", cardNonce.getNonce());
        assertEquals("ending in ••11", cardNonce.getDescription());
        assertEquals("11", cardNonce.getLastTwo());
        assertEquals("1111", cardNonce.getLastFour());
        assertNotNull(cardNonce.getThreeDSecureInfo());
        assertFalse(cardNonce.getThreeDSecureInfo().isLiabilityShifted());
        assertFalse(cardNonce.getThreeDSecureInfo().isLiabilityShiftPossible());
        assertNotNull(cardNonce.getBinData());
        assertEquals(YES, cardNonce.getBinData().getPrepaid());
        assertEquals(YES, cardNonce.getBinData().getHealthcare());
        assertEquals(NO, cardNonce.getBinData().getDebit());
        assertEquals(YES, cardNonce.getBinData().getDurbinRegulated());
        assertEquals(NO, cardNonce.getBinData().getCommercial());
        assertEquals(YES, cardNonce.getBinData().getPayroll());
        assertEquals("Bank of America", cardNonce.getBinData().getIssuingBank());
        assertEquals("USA", cardNonce.getBinData().getCountryOfIssuance());
        assertEquals("123", cardNonce.getBinData().getProductId());
        assertEquals("unregulated",
                cardNonce.getAuthenticationInsight().getRegulationEnvironment());
        assertEquals("01", cardNonce.getExpirationMonth());
        assertEquals("2020", cardNonce.getExpirationYear());
        assertEquals("Joe Smith", cardNonce.getCardholderName());
    }

    @Test
    public void setsCorrectDefaultsWhenValuesAreMissingFromJson() throws JSONException {
        CardNonce cardNonce = CardNonce.fromJson(Fixtures.GRAPHQL_RESPONSE_CREDIT_CARD_MISSING_VALUES);

        assertEquals("", cardNonce.getLastFour());
        assertEquals("", cardNonce.getLastTwo());
        assertEquals("Unknown", cardNonce.getCardType());
        assertEquals("Unknown", cardNonce.getTypeLabel());
        assertNotNull(cardNonce.getThreeDSecureInfo());
        assertEquals("", cardNonce.getBin());
        assertNotNull(cardNonce.getBinData());
        assertEquals("3744a73e-b1ab-0dbd-85f0-c12a0a4bd3d1", cardNonce.getNonce());
        assertEquals("", cardNonce.getDescription());
        assertFalse(cardNonce.isDefault());
        assertNull(cardNonce.getAuthenticationInsight());
        assertEquals("", cardNonce.getExpirationMonth());
        assertEquals("", cardNonce.getExpirationYear());
        assertEquals("", cardNonce.getCardholderName());
    }

    @Test
    public void handlesGraphQLUnknownCardResponses() throws JSONException {
        CardNonce cardNonce = CardNonce.fromJson(Fixtures.GRAPHQL_RESPONSE_UNKNOWN_CREDIT_CARD);

        assertEquals("Unknown", cardNonce.getTypeLabel());
        assertEquals("Unknown", cardNonce.getCardType());
        assertEquals("tokencc_3bbd22_fpjshh_bqbvh5_mkf3nf_smz", cardNonce.getNonce());
        assertEquals("", cardNonce.getDescription());
        assertEquals("", cardNonce.getLastTwo());
        assertEquals("", cardNonce.getLastFour());
        assertEquals("", cardNonce.getExpirationMonth());
        assertEquals("", cardNonce.getExpirationYear());
        assertEquals("", cardNonce.getCardholderName());
        assertNotNull(cardNonce.getThreeDSecureInfo());
        assertFalse(cardNonce.getThreeDSecureInfo().isLiabilityShifted());
        assertFalse(cardNonce.getThreeDSecureInfo().isLiabilityShiftPossible());
        assertNotNull(cardNonce.getBinData());
        assertEquals(UNKNOWN, cardNonce.getBinData().getPrepaid());
        assertEquals(UNKNOWN, cardNonce.getBinData().getHealthcare());
        assertEquals(UNKNOWN, cardNonce.getBinData().getDebit());
        assertEquals(UNKNOWN, cardNonce.getBinData().getDurbinRegulated());
        assertEquals(UNKNOWN, cardNonce.getBinData().getCommercial());
        assertEquals(UNKNOWN, cardNonce.getBinData().getPayroll());
        assertEquals(UNKNOWN, cardNonce.getBinData().getIssuingBank());
        assertEquals(UNKNOWN, cardNonce.getBinData().getCountryOfIssuance());
        assertEquals(UNKNOWN, cardNonce.getBinData().getProductId());
    }

    @Test
    public void parcelsCorrectly() throws JSONException {
        CardNonce cardNonce = CardNonce.fromJson(Fixtures.PAYMENT_METHODS_RESPONSE_VISA_CREDIT_CARD);

        Parcel parcel = Parcel.obtain();
        cardNonce.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);

        CardNonce parceled = CardNonce.CREATOR.createFromParcel(parcel);

        assertEquals("Visa", parceled.getTypeLabel());
        assertEquals("Visa", parceled.getCardType());
        assertEquals("123456-12345-12345-a-adfa", parceled.getNonce());
        assertEquals("ending in ••11", parceled.getDescription());
        assertEquals("11", parceled.getLastTwo());
        assertEquals("1111", parceled.getLastFour());
        assertEquals("01", cardNonce.getExpirationMonth());
        assertEquals("2020", cardNonce.getExpirationYear());
        assertEquals("Joe Smith", cardNonce.getCardholderName());
        assertFalse(parceled.isDefault());
        assertBinDataEqual(cardNonce.getBinData(), parceled.getBinData());
        assertEquals(cardNonce.getAuthenticationInsight().getRegulationEnvironment(),
                parceled.getAuthenticationInsight().getRegulationEnvironment());
    }
}