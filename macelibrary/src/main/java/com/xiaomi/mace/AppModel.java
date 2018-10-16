// Copyright 2018 Xiaomi, Inc.  All rights reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.xiaomi.mace;

import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;


public class AppModel {

    private boolean stopClassify = false;
    private Handler mJniThread;
    public static AppModel instance = new AppModel();
    private AppModel() {
        HandlerThread thread = new HandlerThread("jniThread");
        thread.start();
        mJniThread = new Handler(thread.getLooper());
    }

    public void maceMobilenetCreateGPUContext(final InitData initData) {
        mJniThread.post(new Runnable() {
            @Override
            public void run() {
                int result = JniMaceUtils.maceMobilenetCreateGPUContext(
                        initData.getStoragePath());
                Log.i("APPModel", "maceMobilenetCreateGPUContext result = " + result);
            }
        });
    }

    public void maceMobilenetCreateEngine(final InitData initData, final CreateEngineCallback callback) {
        mJniThread.post(new Runnable() {
            @Override
            public void run() {
                int result = JniMaceUtils.maceMobilenetCreateEngine(
                        initData.getOmpNumThreads(), initData.getCpuAffinityPolicy(),
                        initData.getGpuPerfHint(), initData.getGpuPriorityHint(),
                        initData.getModel(), initData.getDevice());
                Log.i("APPModel", "maceMobilenetCreateEngine result = " + result);

                if (result == -1) {
                    stopClassify = true;

                   /* MaceApp.app.mMainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (callback != null) {
                                callback.onCreateEngineFail(InitData.DEVICES[0].equals(initData.getDevice()));
                            }
                        }
                    });*/
                } else {
                    stopClassify = false;
                }
            }
        });
    }

    public void maceMobilenetClassify(final float[] input) {
        mJniThread.post(new Runnable() {
            @Override
            public void run() {
                if (stopClassify) {
                    return;
                }
                long start = System.currentTimeMillis();
                float[] result = JniMaceUtils.maceMobilenetClassify(input);

              //  final ResultData resultData = LabelCache.instance().getResultFirst(result);
              //  resultData.costTime = System.currentTimeMillis() - start;
             //   EventBus.getDefault().post(new MessageEvent.MaceResultEvent(resultData));
            }
        });
    }

    public interface CreateEngineCallback {
        void onCreateEngineFail(final boolean quit);
    }

}
