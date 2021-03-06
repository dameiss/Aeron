/*
 * Copyright 2015 Kaazing Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.co.real_logic.aeron.tools;

import static org.hamcrest.CoreMatchers.both;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doReturn;

import java.io.BufferedReader;
import java.io.StringReader;

import org.apache.commons.cli.ParseException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class PubSubOptionsTest
{
    private PubSubOptions opts;

    @Before
    public void setUp()
    {
        MockitoAnnotations.initMocks(this);
        opts = new PubSubOptions();
    }

    @Test
    public void help() throws Exception
    {
        final String[] args = {"--help"};
        assertThat(opts.parseArgs(args), is(1));
    }

    @Test
    public void helpShorthand() throws Exception
    {
        final String[] args = {"-h"};
        assertThat(opts.parseArgs(args), is(1));
    }

    @Test
    public void threadsShorthandValid() throws Exception
    {
        final String[] args = {"-t", "1234"};
        opts.parseArgs(args);
        assertThat(opts.threads(), is(1234));
    }

    @Test
    public void threadsLonghandValid() throws Exception
    {
        final String[] args = {"--threads", "1234"};
        opts.parseArgs(args);
        assertThat(opts.threads(), is(1234));
    }

    @Test(expected = ParseException.class)
    public void threadsInvalid() throws Exception
    {
        final String[] args = {"-t", "asdf"};
        opts.parseArgs(args);
    }

    @Test(expected = ParseException.class)
    public void threadsLonghandInvalid() throws Exception
    {
        final String[] args = {"--threads", "-1000"};
        opts.parseArgs(args);
    }

    @Test
    public void iterations() throws Exception
    {
        final String[] args = {"--iterations", "1234"};
        opts.parseArgs(args);
        assertThat(opts.iterations(), is(1234L));
    }

    @Test
    public void messages() throws Exception
    {
        final String[] args = {"--messages", "1234"};
        opts.parseArgs(args);
        assertThat(opts.messages(), is(1234L));
    }

    @Test
    public void messagesShorthand() throws Exception
    {
        final String[] args = {"-m", "1234"};
        opts.parseArgs(args);
        assertThat(opts.messages(), is(1234L));
    }

    @Test
    public void randomSeed() throws Exception
    {
        final String[] args = {"--seed", "1234"};
        opts.parseArgs(args);
        assertThat(opts.randomSeed(), is(1234L));
    }

    @Test
    public void driverEmbedded() throws Exception
    {
        final String[] args = {"--driver", "embedded"};
        opts.parseArgs(args);
        assertThat(opts.useEmbeddedDriver(), is(true));
    }

    @Test
    public void driverExternal() throws Exception
    {
        final String[] args = {"--driver", "external"};
        opts.parseArgs(args);
        assertThat(opts.useEmbeddedDriver(), is(false));
    }

    @Test
    public void inputStreamStdin() throws Exception
    {
        final String[] args = {"--input", "stdin"};
        opts.parseArgs(args);
        assertThat(opts.input(), equalTo(System.in));
    }

    @Test
    public void inputStreamRandomShorthand() throws Exception
    {
        final String[] args = {"-i", "null"};
        opts.parseArgs(args);
        assertThat(opts.input(), is(nullValue()));
    }

    @Test
    public void inputStreamDefault() throws Exception
    {
        final String[] args = {};
        opts.parseArgs(args);
        assertThat("FAIL: default input stream should be null.",
            opts.input(), is(nullValue()));
    }

    @Test
    public void outputStreamDefaultNull() throws Exception
    {
        final String[] args = {};
        opts.parseArgs(args);
        assertThat("FAIL: Default output stream should be null",
            opts.output(), is(nullValue()));
    }

    @Test
    public void outputStreamSetNull() throws Exception
    {
        final String[] args = {"-o", "NULL"};
        opts.parseArgs(args);
        assertThat("FAIL: Output stream should be null when set to the string 'null'",
            opts.output(), is(nullValue()));
    }

    @Test
    public void outputStreamStdout() throws Exception
    {
        final String[] args = {"--output", "StdOut"};
        opts.parseArgs(args);
        assertThat("FAIL: Expected output to be the standard out",
            opts.output(), equalTo(System.out));
    }

    @Test
    public void outputStreamStderr() throws Exception
    {
        final String[] args = {"--output", "stderr"};
        opts.parseArgs(args);
        assertThat("FAIL: Expected output to be standard error.",
            opts.output(), equalTo(System.err));
    }

    @Test
    public void channel() throws Exception
    {
        final String[] args = {"--channels", "udp://127.0.0.1:12345"};
        opts.parseArgs(args);
        assertThat("FAIL: Exactly one channel.",
            opts.channels().size(), is(1));

        final ChannelDescriptor cd = opts.channels().get(0);
        final int[] streamIds = cd.streamIdentifiers();

        assertThat("FAIL: Exactly one stream identifier on the channel.",
            streamIds.length, is(1));
        assertThat("FAIL: Channel is udp://127.0.0.1:12345",
            cd.channel(), is("udp://127.0.0.1:12345"));
        assertThat("FAIL: Stream ID is 1",
            cd.streamIdentifiers()[0], is(1));
    }

    @Test
    public void channelWithStreamId() throws Exception
    {
        final String[] args = {"--channels", "udp://127.0.0.1:12345#100"};
        opts.parseArgs(args);

        assertThat("FAIL: Exactly one channel.",
            opts.channels().size(), is(1));

        final ChannelDescriptor cd = opts.channels().get(0);
        final int[] streamIds = cd.streamIdentifiers();

        assertThat("FAIL: Exactly one stream identifier on the channel.",
            streamIds.length, is(1));
        assertThat("FAIL: Channel is udp://127.0.0.1:12345",
            cd.channel(), is("udp://127.0.0.1:12345"));
        assertThat("FAIL: Stream ID is 100",
            cd.streamIdentifiers()[0], is(100));
    }

    @Test
    public void channelWithPortRange() throws Exception
    {
        final String[] args = {"--channels", "udp://127.0.0.1:12345-12347"};
        opts.parseArgs(args);

        assertThat("FAIL: Expected 3 channels.",
            opts.channels().size(), is(3));
        assertThat("FAIL: Channel 1 incorrect.",
            opts.channels().get(0).channel(), is("udp://127.0.0.1:12345"));
        assertThat("FAIL: Channel 2 incorrect",
            opts.channels().get(1).channel(), is("udp://127.0.0.1:12346"));
        assertThat("FAIL: Channel 3 incorrect",
            opts.channels().get(2).channel(), is("udp://127.0.0.1:12347"));
    }

    @Test
    public void channelWithStreamIdRange() throws Exception
    {
        final String[] args = {"--channels", "udp://127.0.0.1:12345#100-102"};
        opts.parseArgs(args);

        assertThat("FAIL: Expected 1 channel.",
            opts.channels().size(), is(1));
        final ChannelDescriptor cd = opts.channels().get(0);

        assertThat("FAIL: Expected 3 stream IDs on channel.",
            cd.streamIdentifiers().length, is(3));
        assertThat("FAIL: stream-id 1 is wrong value.",
            cd.streamIdentifiers()[0], is(100));
        assertThat("FAIL: Stream-id 2 is wrong value.",
            cd.streamIdentifiers()[1], is(101));
        assertThat("FAIL: Stream-id 3 is wrong value.",
            cd.streamIdentifiers()[2], is(102));
    }

    /**
     * Test that channels can be comma separated values with port and stream id ranges.
     * We should end up with 6 total channels, each with 2 stream-ids
     */
    @Test
    public void channelCsvWithPortAndStreamIdRange() throws Exception
    {
        ChannelDescriptor cd;
        final String[] args = {"--channels",
            "udp://127.0.0.1:5000-5001#1-2,udp://224.9.10.11:6000-6001#600-601,udp://192.168.0.1:7000-7001#700-701"};
        opts.parseArgs(args);

        assertThat("FAIL: Expected 6 channels",
            opts.channels().size(), is(6));

        cd = opts.channels().get(0);
        assertThat("FAIL: Wrong address for channel 1",
            cd.channel(), is("udp://127.0.0.1:5000"));
        assertThat("FAIL: Wrong number of stream IDs on channel 1",
            cd.streamIdentifiers().length, is(2));

        cd = opts.channels().get(1);
        assertThat("FAIL: Wrong address for channel 2",
            cd.channel(), is("udp://127.0.0.1:5001"));
        assertThat("FAIL: Wrong number of stream IDs on channel 2",
            cd.streamIdentifiers().length, is(2));

        cd = opts.channels().get(2);
        assertThat("FAIL: Wrong address for channel 3",
            cd.channel(), is("udp://224.9.10.11:6000"));
        assertThat("FAIL: Wrong number of stream IDs on channel 3",
            cd.streamIdentifiers().length, is(2));

        cd = opts.channels().get(3);
        assertThat("FAIL: Wrong address for channel 4",
            cd.channel(), is("udp://224.9.10.11:6001"));
        assertThat("FAIL: Wrong number of stream IDs on channel 4",
            cd.streamIdentifiers().length, is(2));

        cd = opts.channels().get(4);
        assertThat("FAIL: Wrong address for channel 5",
            cd.channel(), is("udp://192.168.0.1:7000"));
        assertThat("FAIL: Wrong number of stream IDs on channel 5",
            cd.streamIdentifiers().length, is(2));

        cd = opts.channels().get(5);
        assertThat("FAIL: Wrong address for channel 6",
            cd.channel(), is("udp://192.168.0.1:7001"));
        assertThat("FAIL: Wrong number of stream IDs on channel 6",
            cd.streamIdentifiers().length, is(2));
    }

    @Test
    public void channelIPv6() throws Exception
    {
        ChannelDescriptor cd;
        final String[] args = {"--channels", "udp://[fe80::1234:2345:3456:4567]:12345#5"};
        opts.parseArgs(args);

        assertThat("FAIL: Expected 1 channel",
            opts.channels().size(), is(1));
        cd = opts.channels().get(0);
        assertThat("FAIL wrong address for channel 1",
            cd.channel(), is("udp://[fe80::1234:2345:3456:4567]:12345"));
    }

    @Test
    public void channelAeronUnicastIPv4() throws Exception
    {
        ChannelDescriptor cd;
        final String[] args = {"--channels", "aeron:udp?remote=192.168.14.101:10000-10001|local=192.168.14.102#5"};
        opts.parseArgs(args);

        assertThat(opts.channels().size(), is(2));
        cd = opts.channels().get(0);
        assertThat("FAIL: wrong address for channel 1",
            cd.channel(), is("aeron:udp?remote=192.168.14.101:10000|local=192.168.14.102"));
        cd = opts.channels().get(1);
        assertThat("FAIL: wrong address for channel 2",
            cd.channel(), is("aeron:udp?remote=192.168.14.101:10001|local=192.168.14.102"));
    }

    @Test
    public void channelAeronUnicastIPv6() throws Exception
    {
        ChannelDescriptor cd;
        final String[] args = {"--channels", "aeron:udp?remote=[::1]:12345|local=[::1]"};
        opts.parseArgs(args);

        assertThat(opts.channels().size(), is(1));
        cd = opts.channels().get(0);
        assertThat("FAIL: wrong address for channel 1",
            cd.channel(), is("aeron:udp?remote=[::1]:12345|local=[::1]"));
    }

    @Test
    public void numberOfStreams() throws Exception
    {
        final String[] args = {"--channels", "udp://localhost:12345#1-10,udp://localhost:12345#11"};
        opts.parseArgs(args);

        assertThat(opts.numberOfStreams(), is(11));
    }

    @Test
    public void numberOfStreamsAeronUri() throws Exception
    {
        final String[] args =
            {
                "--channels",
                "aeron:udp?group=224.10.20.21:12345#1-10|interface=127.0.0.1," +
                    "aeron:udp?remote=localhost:13333#1|local=127.0.0.1"
            };

        opts.parseArgs(args);
        assertThat(opts.numberOfStreams(), is(11));
    }

    @Test
    public void messageSizes() throws Exception
    {
        final String[] args = {"--size", "100"};
        opts.parseArgs(args);
        final MessageSizePattern p = opts.messageSizePattern();
        assertThat(p.getNext(), is(100));
    }

    @Test
    public void messageSizeShortHand() throws Exception
    {
        final String[] args = {"-s", "100"};
        opts.parseArgs(args);
        final MessageSizePattern p = opts.messageSizePattern();
        assertThat(p.getNext(), is(100));
    }

    @Test
    public void messageSizeRange() throws Exception
    {
        final String[] args = {"--size", "101-102"};
        opts.parseArgs(args);
        final MessageSizePattern p = opts.messageSizePattern();
        assertThat(p.getNext(), both(greaterThanOrEqualTo(101)).and(lessThanOrEqualTo(102)));
    }

    @Test
    public void messageNumberAndSize() throws Exception
    {
        final String[] args = {"--size", "1@100"};
        opts.parseArgs(args);
        final MessageSizePattern p = opts.messageSizePattern();
        assertThat(p.getNext(), is(100));
    }

    @Test
    public void messageNumberAndRange() throws Exception
    {
        final String[] args = {"--size", "1@101-102"};
        opts.parseArgs(args);
        final MessageSizePattern p = opts.messageSizePattern();
        assertThat(p.getNext(), both(greaterThanOrEqualTo(101)).and(lessThanOrEqualTo(102)));
    }

    @Test
    public void messageNumberAndRangeCsv() throws Exception
    {
        final String[] args = {"--size", "1@100,1@101-102,98@1000"};
        opts.parseArgs(args);
        final MessageSizePattern p = opts.messageSizePattern();
        assertThat(p.getNext(), is(100));
        assertThat(p.getNext(), both(greaterThanOrEqualTo(101)).and(lessThanOrEqualTo(102)));
        assertThat(p.getNext(), is(1000));
    }

    @Test
    public void messageSizeBytesSuffix() throws Exception
    {
        final String[] args = {"--size", "1@100B,1@101b"};
        opts.parseArgs(args);
        final MessageSizePattern p = opts.messageSizePattern();
        assertThat(p.getNext(), is(100));
        assertThat(p.getNext(), is(101));
    }

    @Test
    public void messageSizeKilobytesSuffix() throws Exception
    {
        final String[] args = {"--size", "1@100K,1@101k,1@102KB,1@103kb"};
        opts.parseArgs(args);
        final MessageSizePattern p = opts.messageSizePattern();
        assertThat(p.getNext(), is(100 * 1024));
        assertThat(p.getNext(), is(101 * 1024));
        assertThat(p.getNext(), is(102 * 1024));
        assertThat(p.getNext(), is(103 * 1024));
    }

    @Test
    public void messageSizeMegabytesSuffix() throws Exception
    {
        final String[] args = {"--size", "1@100M,1@101m,1@102MB,1@103mb"};
        opts.parseArgs(args);
        final MessageSizePattern p = opts.messageSizePattern();
        assertThat(p.getNext(), is(100 * 1024 * 1024));
        assertThat(p.getNext(), is(101 * 1024 * 1024));
        assertThat(p.getNext(), is(102 * 1024 * 1024));
        assertThat(p.getNext(), is(103 * 1024 * 1024));
    }

    @Test
    public void messageSizeRangesWithSuffixes() throws Exception
    {
        final String[] args = {"--size", "1@1023B-1KB,1@1023KB-1MB"};
        opts.parseArgs(args);
        final MessageSizePattern p = opts.messageSizePattern();
        assertThat(p.getNext(), both(greaterThanOrEqualTo(1023)).and(lessThanOrEqualTo((1024))));
        assertThat(p.getNext(), both(greaterThanOrEqualTo(1023 * 1024)).and(lessThanOrEqualTo(1024 * 1024)));
    }

    @Test
    public void rateMaximum() throws Exception
    {
        final String[] args = {"--rate", "max"};
        opts.parseArgs(args);
        assertThat(opts.rateIntervals().size(), is(1));

        final RateControllerInterval interval = opts.rateIntervals().get(0);
        assertThat(interval, instanceOf(SecondsAtBitsPerSecondInterval.class));
        final SecondsAtBitsPerSecondInterval sub = (SecondsAtBitsPerSecondInterval) interval;
        assertThat(sub.seconds(), is((double) Long.MAX_VALUE));
        assertThat(sub.bitsPerSecond(), is(Long.MAX_VALUE));
    }

    @Test
    public void rateMbps() throws Exception
    {
        final String[] args = {"--rate", "1Gbps"};
        opts.parseArgs(args);
        assertThat(opts.rateIntervals().size(), is(1));

        final RateControllerInterval interval = opts.rateIntervals().get(0);
        assertThat(interval, instanceOf(SecondsAtBitsPerSecondInterval.class));
        final SecondsAtBitsPerSecondInterval sub = (SecondsAtBitsPerSecondInterval) interval;
        assertThat(sub.seconds(), is((double) Long.MAX_VALUE));
        assertThat(sub.bitsPerSecond(), is(1_000_000_000L));
    }

    @Test
    public void rateMessagesPerSecond() throws Exception
    {
        final String[] args = {"--rate", "100mps"};
        opts.parseArgs(args);
        assertThat(opts.rateIntervals().size(), is(1));

        final RateControllerInterval interval = opts.rateIntervals().get(0);
        assertThat(interval, instanceOf(SecondsAtMessagesPerSecondInterval.class));
        final SecondsAtMessagesPerSecondInterval sub = (SecondsAtMessagesPerSecondInterval) interval;
        assertThat(sub.seconds(), is((double) Long.MAX_VALUE));
        assertThat(sub.messagesPerSecond(), is(100D));
    }

    @Test(expected = ParseException.class)
    public void rateNotValid() throws Exception
    {
        final String[] args = {"--rate", "1.21 Giga Watts"};
        opts.parseArgs(args); // will throw ParseException
    }

    @Test
    public void rateMessagesAtBitsPerSecond() throws Exception
    {
        final String[] args = {"--rate", "100m@0.5Mbps"};
        opts.parseArgs(args);
        assertThat(opts.rateIntervals().size(), is(1));

        final RateControllerInterval interval = opts.rateIntervals().get(0);
        assertThat(interval, instanceOf(MessagesAtBitsPerSecondInterval.class));
        final MessagesAtBitsPerSecondInterval sub = (MessagesAtBitsPerSecondInterval) interval;
        assertThat(sub.messages(), is(100L));
        assertThat(sub.bitsPerSecond(), is(500000L));
    }

    @Test
    public void rateMessagesAtMessagesPerSecond() throws Exception
    {
        final String[] args = {"--rate", "10m@1mps"};
        opts.parseArgs(args);
        assertThat(opts.rateIntervals().size(), is(1));

        final RateControllerInterval interval = opts.rateIntervals().get(0);
        assertThat(interval, instanceOf(MessagesAtMessagesPerSecondInterval.class));
        final MessagesAtMessagesPerSecondInterval sub = (MessagesAtMessagesPerSecondInterval) interval;
        assertThat(sub.messages(), is(10L));
        assertThat(sub.messagesPerSecond(), is(1D));
    }

    @Test
    public void rateSecondsAtBitsPerSecond() throws Exception
    {
        final String[] args = {"--rate", "10.4s@1.5Kbps"};
        opts.parseArgs(args);
        assertThat(opts.rateIntervals().size(), is(1));

        final RateControllerInterval interval = opts.rateIntervals().get(0);
        assertThat(interval, instanceOf(SecondsAtBitsPerSecondInterval.class));
        final SecondsAtBitsPerSecondInterval sub = (SecondsAtBitsPerSecondInterval) interval;
        assertThat(sub.seconds(), is(10.4D));
        assertThat(sub.bitsPerSecond(), is(1500L));
    }

    @Test
    public void rateSecondsAtMessagesPerSecond() throws Exception
    {
        final String[] args = {"--rate", "11s@100mps"};
        opts.parseArgs(args);
        assertThat(opts.rateIntervals().size(), is(1));

        final RateControllerInterval interval = opts.rateIntervals().get(0);
        assertThat(interval, instanceOf(SecondsAtMessagesPerSecondInterval.class));
        final SecondsAtMessagesPerSecondInterval sub = (SecondsAtMessagesPerSecondInterval) interval;
        assertThat(sub.seconds(), is(11D));
        assertThat(sub.messagesPerSecond(), is(100D));
    }

    @Test
    public void rateCsv() throws Exception
    {
        final String[] args = {"--rate", "100m@0.5mps,10s@1.5Mbps,10s@100mps,50m@100bps"};
        opts.parseArgs(args);
        assertThat(opts.rateIntervals().size(), is(4));

        RateControllerInterval interval = opts.rateIntervals().get(0);
        assertThat(interval, instanceOf(MessagesAtMessagesPerSecondInterval.class));
        final MessagesAtMessagesPerSecondInterval sub1 = (MessagesAtMessagesPerSecondInterval) interval;
        assertThat(sub1.messages(), is(100L));
        assertThat(sub1.messagesPerSecond(), is(0.5D));

        interval = opts.rateIntervals().get(1);
        assertThat(interval, instanceOf(SecondsAtBitsPerSecondInterval.class));
        final SecondsAtBitsPerSecondInterval sub2 = (SecondsAtBitsPerSecondInterval) interval;
        assertThat(sub2.seconds(), is(10D));
        assertThat(sub2.bitsPerSecond(), is(1_500_000L));

        interval = opts.rateIntervals().get(2);
        assertThat(interval, instanceOf(SecondsAtMessagesPerSecondInterval.class));
        final SecondsAtMessagesPerSecondInterval sub3 = (SecondsAtMessagesPerSecondInterval) interval;
        assertThat(sub3.seconds(), is(10D));
        assertThat(sub3.messagesPerSecond(), is(100D));

        interval = opts.rateIntervals().get(3);
        assertThat(interval, instanceOf(MessagesAtBitsPerSecondInterval.class));
        final MessagesAtBitsPerSecondInterval sub4 = (MessagesAtBitsPerSecondInterval) interval;
        assertThat(sub4.messages(), is(50L));
        assertThat(sub4.bitsPerSecond(), is(100L));
    }

    @Test
    public void sessionId() throws Exception
    {
        final String[] args = {"--session", "1000"};
        assertThat("FAIL: useSessionId needs to default to false",
            opts.useSessionId(), is(false));
        opts.parseArgs(args);
        assertThat(opts.sessionId(), is(1000));
        assertThat("FAIL: After successfully setting a session ID, useSessionId should return true.",
            opts.useSessionId(), is(true));
    }

    @Test
    public void sessionIdDefault() throws Exception
    {
        final String[] args = {"--session", "default"};
        opts.parseArgs(args);
        assertThat("FAIL: using default session ID means useSessionId should return false.",
            opts.useSessionId(), is(false));
    }

    @Test
    public void sessionIdFailure()
    {
        final String[] args = {"--session", "fails"};
        boolean exceptionThrown = false;
        opts.useSessionId(true);
        try
        {
            opts.parseArgs(args);
        }
        catch (final ParseException ex)
        {
            exceptionThrown = true;
        }
        assertThat("FAIL: parseArgs should have thrown a ParseException for malformed session ID",
            exceptionThrown, is(true));
        assertThat("FAIL: invalid session ID input should cause useSessionId to return false.",
            opts.useSessionId(), is(false));
    }

    @Test
    public void verifyOn() throws Exception
    {
        final String[] args = {"--verify", "Yes"};
        opts.parseArgs(args);
        assertThat(opts.verify(), is(true));
    }

    @Test
    public void verifyOff() throws Exception
    {
        final String[] args = {"--verify", "No"};
        opts.parseArgs(args);
        assertThat(opts.verify(), is(false));
    }

    @Test
    public void verifyDefault() throws Exception
    {
        final String[] args = {};
        opts.parseArgs(args);
        assertThat("FAIL: Default for --verify should be true",
            opts.verify(), is(true));
    }

    @Test(expected = ParseException.class)
    public void verifyException() throws Exception
    {
        final String[] args = {"--verify", "schmerify"};
        opts.parseArgs(args);
    }

    @Test
    public void defaultsFile() throws Exception
    {
        // Use a mockito spy to return our own BufferedReader from the helper function
        final PubSubOptions spyOpts = Mockito.spy(opts);
        final String text = "-c udp://192.168.0.100:3000#5";
        final StringReader reader = new StringReader(text);
        final BufferedReader br = new BufferedReader(reader);

        // Make the function return the buffered reader we just created.
        doReturn(br).when(spyOpts).newBufferedFileReader("filename");
        final String[] args = {"--defaults", "filename"};
        spyOpts.parseArgs(args);

        // Channel should be what was in the file
        assertThat(spyOpts.channels().size(), is(1));
        final ChannelDescriptor cd = spyOpts.channels().get(0);

        assertThat(cd.channel(), is("udp://192.168.0.100:3000"));
        assertThat(cd.streamIdentifiers().length, is(1));
        assertThat(cd.streamIdentifiers()[0], is(5));
    }

    @Test
    public void defaultsFileMultipleLines() throws Exception
    {
        // see defaultsFile() test for comments about what's going on here.
        final PubSubOptions spyOpts = Mockito.spy(opts);
        final String nl = System.lineSeparator();
        final String text = "# This is a comment line and will be ignored." + nl +
            "   " + nl +              // line with spaces is ignored
            "-s 100 -r 100mps" + nl + // multiple options on one line works
            "       -t 3     " + nl + // leading and trailing whitespace ignored
            "--verify      no" + nl + // number of spaces between entries doesn't matter
            "-o stdout" + nl +      // a newline will separate options
            nl; // empty line ignored
        final StringReader reader = new StringReader(text);
        final BufferedReader br = new BufferedReader(reader);
        doReturn(br).when(spyOpts).newBufferedFileReader("textfile");

        final String[] args = {"--defaults", "textfile"};
        spyOpts.parseArgs(args);

        // check size
        assertThat("FAIL: Minimum size of messages incorrect.",
            spyOpts.messageSizePattern().minimum(), is(100));

        // check rate
        assertThat(spyOpts.rateIntervals().size(), is(1));
        assertThat(spyOpts.rateIntervals().get(0), instanceOf(SecondsAtMessagesPerSecondInterval.class));
        final SecondsAtMessagesPerSecondInterval sub = (SecondsAtMessagesPerSecondInterval) spyOpts.rateIntervals().get(0);
        assertThat(sub.seconds(), is((double) Long.MAX_VALUE));
        assertThat(sub.messagesPerSecond(), is(100D));

        // check threads
        assertThat(spyOpts.threads(), is(3));

        // check verify
        assertThat(spyOpts.verify(), is(false));

        // check output
        assertThat(spyOpts.output(), equalTo(System.out));
    }

    @Test
    public void defaultsOverriddenByCmdline() throws Exception
    {
        // the --defaults become the new default values which can be overridden as normal
        // by anything else on the command line regardless of order
        final PubSubOptions spyOpts = Mockito.spy(opts);
        final String text = "--verify no --threads 2 --output stdout";
        final BufferedReader br = new BufferedReader(new StringReader(text));
        doReturn(br).when(spyOpts).newBufferedFileReader("filename");

        final String[] args = {"--output", "stderr", "--defaults", "filename", "--verify", "yes"};
        spyOpts.parseArgs(args);

        assertThat("FAIL: Should be overridden by args",
            spyOpts.verify(), is(true));
        assertThat("FAIL: Should be picked up from the defaults file",
            spyOpts.threads(), is(2));
        assertThat("FAIL: Should be overridden by args",
            spyOpts.output(), equalTo(System.err));
    }
}
