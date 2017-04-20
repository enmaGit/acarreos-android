package com.acarreos.creative.Models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by EnmanuelPc on 26/09/2015.
 */
public class AddressModel {

    @SerializedName("formatted_address")
    public String formattedAddress;

    public String getFormattedAddress() {
        return formattedAddress;
    }

    public void setFormattedAddress(String formattedAddress) {
        this.formattedAddress = formattedAddress;
    }
}
