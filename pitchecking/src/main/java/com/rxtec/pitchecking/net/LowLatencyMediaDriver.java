package com.rxtec.pitchecking.net;

import org.agrona.concurrent.BackoffIdleStrategy;
import org.agrona.concurrent.BusySpinIdleStrategy;
import org.agrona.concurrent.SigIntBarrier;

import io.aeron.driver.MediaDriver;
import io.aeron.driver.ThreadingMode;

public class LowLatencyMediaDriver {

	public static void main(final String[] args) throws Exception
    {
        MediaDriver.loadPropertiesFiles(args);

        final MediaDriver.Context ctx = new MediaDriver.Context()
            .threadingMode(ThreadingMode.DEDICATED)
            .conductorIdleStrategy(new BackoffIdleStrategy(1, 1, 1, 1))
            .receiverIdleStrategy(new BusySpinIdleStrategy())
            .senderIdleStrategy(new BusySpinIdleStrategy());

        try (final MediaDriver ignored = MediaDriver.launch(ctx))
        {
            new SigIntBarrier().await();

            System.out.println("Shutdown Driver...");
        }
    }
}
