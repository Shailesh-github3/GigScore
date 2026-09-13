package com.org.gigscore.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import com.org.gigscore.exception.BadRequestException;

class PlatformTest {

    @Test
    void normalize_acceptsKnownPlatforms() {
        assertThat(Platform.normalize("Uber")).isEqualTo("Uber");
        assertThat(Platform.normalize("Upwork")).isEqualTo("Upwork");
        assertThat(Platform.normalize("Swiggy")).isEqualTo("Swiggy");
    }

    @Test
    void normalize_isCaseAndSpacingInsensitive() {
        assertThat(Platform.normalize("  upwork ")).isEqualTo("Upwork");
        assertThat(Platform.normalize("TASKRABBIT")).isEqualTo("TaskRabbit");
        assertThat(Platform.normalize("taskrabbit")).isEqualTo("TaskRabbit");
    }

    @Test
    void normalize_rejectsUnknownOrMissingPlatforms() {
        assertThrows(BadRequestException.class, () -> Platform.normalize("MySpace"));
        assertThrows(BadRequestException.class, () -> Platform.normalize("  "));
        assertThrows(BadRequestException.class, () -> Platform.normalize(null));
    }

    @Test
    void allowedValues_listsDisplayNames() {
        assertThat(Platform.allowedValues()).contains("Uber", "Upwork", "Swiggy", "Zomato", "Rapido", "Other");
    }
}