package painting.hungershield.config;

import com.google.gson.annotations.SerializedName;

public final class HungerShieldConfig {
    @SerializedName("hunger_shield")
    private boolean hungerShieldEnabled = true;

    public boolean isHungerShieldEnabled() {
        return this.hungerShieldEnabled;
    }

    public void setHungerShieldEnabled(boolean hungerShieldEnabled) {
        this.hungerShieldEnabled = hungerShieldEnabled;
    }
}
