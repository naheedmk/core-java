/*
 * Copyright 2017, TeamDev Ltd. All rights reserved.
 *
 * Redistribution and use in source and/or binary forms, with or without
 * modification, must retain the above copyright notice and the following
 * disclaimer.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.spine3.time.change;

import com.google.common.testing.NullPointerTester;
import com.google.protobuf.Timestamp;
import org.junit.Test;
import org.spine3.test.TimeTests;
import org.spine3.time.Interval;
import org.spine3.time.Intervals;
import org.spine3.time.LocalDate;
import org.spine3.time.LocalDates;
import org.spine3.time.LocalTime;
import org.spine3.time.LocalTimes;
import org.spine3.time.OffsetDate;
import org.spine3.time.OffsetDateTime;
import org.spine3.time.OffsetDateTimes;
import org.spine3.time.OffsetDates;
import org.spine3.time.OffsetTime;
import org.spine3.time.OffsetTimes;
import org.spine3.time.ZoneOffset;
import org.spine3.time.ZoneOffsets;

import static com.google.protobuf.util.Timestamps.subtract;
import static org.junit.Assert.assertEquals;
import static org.spine3.test.Tests.assertHasPrivateParameterlessCtor;
import static org.spine3.time.Durations2.minutes;
import static org.spine3.time.Timestamps2.getCurrentTime;

@SuppressWarnings({"ConstantConditions" /* We pass `null` to some of the methods to check
                                           handling of preconditions */,
                   "ResultOfMethodCallIgnored" /* ...when methods throw exceptions */,
                   "ClassWithTooManyMethods",
                   "OverlyCoupledClass" /* we test many data types and utility methods */})
public class TimeChangesShould {

    @Test
    public void have_private_constructor() {
        assertHasPrivateParameterlessCtor(TimeChanges.class);
    }

    @Test(expected = NullPointerException.class)
    public void do_not_accept_null_Interval_previousValue() {
        final Timestamp fourMinutesAgo = TimeTests.Past.minutesAgo(4);
        final Timestamp now = getCurrentTime();
        final Interval fourMinutes = Intervals.between(fourMinutesAgo, now);
        TimeChanges.of(null, fourMinutes);
    }

    @Test(expected = NullPointerException.class)
    public void do_not_accept_null_Interval_newValue() {
        final Timestamp fourMinutesAgo = TimeTests.Past.minutesAgo(4);
        final Timestamp now = getCurrentTime();
        final Interval fourMinutes = Intervals.between(fourMinutesAgo, now);
        TimeChanges.of(fourMinutes, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void do_not_accept_equal_Interval_values() {
        final Timestamp fourMinutesAgo = TimeTests.Past.minutesAgo(4);
        final Timestamp now = getCurrentTime();
        final Interval fourMinutes = Intervals.between(now, fourMinutesAgo);
        TimeChanges.of(fourMinutes, fourMinutes);
    }

    @Test
    public void create_IntervalChange_instance() {
        final Timestamp fiveMinutesAgo = TimeTests.Past.minutesAgo(5);
        final Timestamp fourMinutesAgo = TimeTests.Past.minutesAgo(4);
        final Timestamp now = getCurrentTime();
        final Interval fourMinutes = Intervals.between(fourMinutesAgo, now);
        final Interval fiveMinutes = Intervals.between(fiveMinutesAgo, now);

        final IntervalChange result = TimeChanges.of(fourMinutes, fiveMinutes);

        assertEquals(fourMinutes, result.getPreviousValue());
        assertEquals(fiveMinutes, result.getNewValue());
    }

    @Test(expected = NullPointerException.class)
    public void do_not_accept_null_LocalDate_previousValue() {
        final LocalDate today = LocalDates.now();
        TimeChanges.of(null, today);
    }

    @Test(expected = NullPointerException.class)
    public void do_not_accept_null_LocalDate_newValue() {
        final LocalDate today = LocalDates.now();
        TimeChanges.of(today, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void do_not_accept_equal_LocalDate_values() {
        final LocalDate today = LocalDates.now();
        TimeChanges.of(today, today);
    }

    @Test
    public void create_LocalDateChange_instance() {
        final LocalDate today = LocalDates.now();
        final LocalDate tomorrow = LocalDates.plusDays(today, 1);

        final LocalDateChange result = TimeChanges.of(today, tomorrow);

        assertEquals(today, result.getPreviousValue());
        assertEquals(tomorrow, result.getNewValue());
    }

    @Test(expected = NullPointerException.class)
    public void do_not_accept_null_LocalTime_previousValue() {
        final LocalTime now = LocalTimes.now();
        TimeChanges.of(null, now);
    }

    @Test(expected = NullPointerException.class)
    public void do_not_accept_null_LocalTime_newValue() {
        final LocalTime now = LocalTimes.now();
        TimeChanges.of(now, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void do_not_accept_equal_LocalTime_values() {
        final LocalTime now = LocalTimes.now();
        TimeChanges.of(now, now);
    }

    @Test
    public void create_LocalTimeChange_instance() {
        final LocalTime now = LocalTimes.now();
        final LocalTime inFiveHours = LocalTimes.addHours(now, 5);

        final LocalTimeChange result = TimeChanges.of(now, inFiveHours);

        assertEquals(now, result.getPreviousValue());
        assertEquals(inFiveHours, result.getNewValue());
    }

    @Test(expected = NullPointerException.class)
    public void do_not_accept_null_OffsetDate_previousValue() {
        final ZoneOffset inLassVegas = ZoneOffsets.ofHours(8);
        final OffsetDate date = OffsetDates.now(inLassVegas);
        TimeChanges.of(null, date);
    }

    @Test(expected = NullPointerException.class)
    public void do_not_accept_null_OffsetDate_newValue() {
        final ZoneOffset inKiev = ZoneOffsets.ofHours(3);
        final OffsetDate date = OffsetDates.now(inKiev);
        TimeChanges.of(date, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void do_not_accept_equal_OffsetDate_values() {
        final ZoneOffset inLuxembourg = ZoneOffsets.ofHours(1);
        final OffsetDate date = OffsetDates.now(inLuxembourg);
        TimeChanges.of(date, date);
    }

    @Test
    public void create_OffsetDateChange_instance() {
        final ZoneOffset inKiev = ZoneOffsets.ofHours(3);
        final ZoneOffset inLuxembourg = ZoneOffsets.ofHours(1);
        final OffsetDate previousDate = OffsetDates.now(inKiev);
        final OffsetDate newDate = OffsetDates.now(inLuxembourg);

        final OffsetDateChange result = TimeChanges.of(previousDate, newDate);

        assertEquals(previousDate, result.getPreviousValue());
        assertEquals(newDate, result.getNewValue());
    }

    @Test(expected = NullPointerException.class)
    public void do_not_accept_null_OffsetTime_previousValue() {
        final ZoneOffset inLassVegas = ZoneOffsets.ofHours(8);
        final OffsetTime now = OffsetTimes.now(inLassVegas);
        TimeChanges.of(null, now);
    }

    @Test(expected = NullPointerException.class)
    public void do_not_accept_null_OffsetTime_newValue() {
        final ZoneOffset inKiev = ZoneOffsets.ofHours(3);
        final OffsetTime now = OffsetTimes.now(inKiev);
        TimeChanges.of(now, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void do_not_accept_equal_OffsetTime_values() {
        final ZoneOffset inLuxembourg = ZoneOffsets.ofHours(1);
        final OffsetTime now = OffsetTimes.now(inLuxembourg);
        TimeChanges.of(now, now);
    }

    @Test
    public void create_OffsetTimeChange_instance() {
        final ZoneOffset inKiev = ZoneOffsets.ofHours(3);
        final ZoneOffset inLuxembourg = ZoneOffsets.ofHours(1);
        final OffsetTime previousTime = OffsetTimes.now(inKiev);
        final OffsetTime newTime = OffsetTimes.now(inLuxembourg);

        final OffsetTimeChange result = TimeChanges.of(previousTime, newTime);

        assertEquals(previousTime, result.getPreviousValue());
        assertEquals(newTime, result.getNewValue());
    }

    @Test(expected = NullPointerException.class)
    public void do_not_accept_null_OffsetDateTime_previousValue() {
        final ZoneOffset inLassVegas = ZoneOffsets.ofHours(8);
        final OffsetDateTime now = OffsetDateTimes.now(inLassVegas);
        TimeChanges.of(null, now);
    }

    @Test(expected = NullPointerException.class)
    public void do_not_accept_null_OffsetDateTime_newValue() {
        final ZoneOffset inKiev = ZoneOffsets.ofHours(3);
        final OffsetDateTime now = OffsetDateTimes.now(inKiev);
        TimeChanges.of(now, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void do_not_accept_equal_OffsetDateTime_values() {
        final ZoneOffset inLuxembourg = ZoneOffsets.ofHours(1);
        final OffsetDateTime now = OffsetDateTimes.now(inLuxembourg);
        TimeChanges.of(now, now);
    }

    @Test
    public void create_OffsetDateTimeChange_instance() {
        final ZoneOffset inKiev = ZoneOffsets.ofHours(3);
        final ZoneOffset inLuxembourg = ZoneOffsets.ofHours(1);
        final OffsetDateTime previousDateTime = OffsetDateTimes.now(inKiev);
        final OffsetDateTime newDateTime = OffsetDateTimes.now(inLuxembourg);

        final OffsetDateTimeChange result = TimeChanges.of(previousDateTime, newDateTime);

        assertEquals(previousDateTime, result.getPreviousValue());
        assertEquals(newDateTime, result.getNewValue());
    }

    @Test
    public void pass_the_null_tolerance_check() {
        new NullPointerTester()
                .setDefault(OffsetTime.class, OffsetTimes.now(ZoneOffsets.UTC))
                .setDefault(OffsetDate.class, OffsetDates.now(ZoneOffsets.UTC))
                .setDefault(OffsetDateTime.class, OffsetDateTimes.now(ZoneOffsets.UTC))
                .setDefault(LocalDate.class, LocalDates.now())
                .setDefault(LocalTime.class, LocalTimes.now())
                .setDefault(Interval.class,
                            Intervals.between(subtract(getCurrentTime(), minutes(1)),
                                              getCurrentTime()))
                .testAllPublicStaticMethods(TimeChanges.class);
    }
}
