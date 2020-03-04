package client.NewElectric.app10000.modules.main.tabPages.monitor;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Environment;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.text.TextUtils;
import android.view.View;

import com.app10000.apps.strategylibrary.config.MachineVersion;
import com.app10000.apps.strategylibrary.dispatchers.DispatcherManager;
import com.app10000.apps.strategylibrary.strategies._base.NodeStrategy;
import com.app10000.apps.strategylibrary.strategies.activation.Action485;
import com.app10000.apps.strategylibrary.strategies.battery.BatteryInfo;
import com.app10000.apps.strategylibrary.strategies.charging.ChargingStrategy;
import com.app10000.apps.strategylibrary.strategies.charging.OnChargingAction;
import com.app10000.apps.strategylibrary.strategies.checkCode.CheckCodeStrategy;
import com.app10000.apps.strategylibrary.strategies.pushRod.OnPushAction;
import com.app10000.apps.strategylibrary.strategies.pushRod.PushRodStrategy;
import com.app10000.apps.strategylibrary.strategies.relay.RelayCloseStrategy;
import com.app10000.apps.strategylibrary.strategies.relay.RelayOpenStrategy;
import com.app10000.apps.strategylibrary.strategies.upgrade.battery.BatteryUpgradeStrategy;
import com.app10000.apps.strategylibrary.strategies.upgrade.battery.BoQiang.BoQiangBatteryUpgradeStrategy;
import com.app10000.apps.strategylibrary.strategies.upgrade.battery.ChaoLiYuan.ChaoLiYuanBatteryUpgradeStrategy;
import com.app10000.apps.strategylibrary.strategies.upgrade.battery.GuanTong.GuanTongBatteryUpgradeStrategy;
import com.app10000.apps.strategylibrary.strategies.upgrade.battery.NuoWan.NuoWanBatteryUpgradeStrategy;
import com.app10000.apps.strategylibrary.strategies.upgrade.dc.ACDC_UpgradeStrategy;
import com.app10000.apps.strategylibrary.strategies.upgrade.dc.DCDC_UpgradeStrategy;
import com.app10000.apps.strategylibrary.strategies.upgrade.pdu.PduUpgradeStrategy;
import com.orhanobut.logger.Logger;

import java.io.File;
import java.io.IOException;
import java.util.Random;

import androidx.annotation.Nullable;
import androidx.core.util.Consumer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.OnClick;
import client.NewElectric.app10000.FileUtil;
import client.NewElectric.app10000.R;
import client.NewElectric.app10000._base.fragments.AppBaseFragment;
import client.NewElectric.app10000.modules.config.MachineVersionConfig;
import client.NewElectric.app10000.modules.main.tabPages.monitor.adapter.MonitorAdapter;
import client.NewElectric.app10000.modules.main.viewmodel.BatteryViewModel;
import client.NewElectric.app10000.widgets.SimpleItemDecoration;
import client.NewElectric.app10000.widgets.dialog.AppDialog;

/**
 * Author:      Lee Yeung
 * Create Date: 2019-09-29
 * Description:
 */
public class MonitorFragment extends AppBaseFragment implements View.OnClickListener
{
    @BindView(R.id.rv_monitor)
    RecyclerView rvMonitor;
    private MonitorAdapter monitorAdapter;
    private AppDialog appDialog = new AppDialog(R.layout.layout_loading_dialog);
    private MonitorAdapter.OnOpenDoorAction onOpenDoorAction = address -> {
        System.out.println("推杆地址：" + address);
        // TODO: 2019-10-09 执行推杆操作
        pushRod(address);
    };

    private BatteryViewModel batteryViewModel;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        monitorAdapter = new MonitorAdapter(getContext());
        monitorAdapter.setOnOpenDoorAction(onOpenDoorAction);
        setMachineVersion(MachineVersionConfig.getMachineVersion());
        rvMonitor.setLayoutManager(new GridLayoutManager(getContext(), 3));
        rvMonitor.addItemDecoration(new SimpleItemDecoration());
        rvMonitor.setAdapter(monitorAdapter);

        batteryViewModel = ViewModelProviders.of(getActivity()).get(BatteryViewModel.class);
        if (batteryViewModel.batteryMonitorLiveData != null)
        {
            batteryViewModel.batteryMonitorLiveData.observe(this, batteryData -> {
                if (monitorAdapter != null)
                {
                    if (batteryData instanceof BatteryInfo)
                    {
                        checkLocked((BatteryInfo) batteryData);
                    }
                    monitorAdapter.addElement(batteryData);
                }
            });
        }
        //                testBatteryUpgradeStrategy();
//        testNulWanBatteryUpgradeStrategy_can();
        //                                test();
        //        testBoQiangBatteryUpgradeStrategy();
        //        testChaoLiYuanUpgradeStrategy();
        //                        testCheckCode();
        //        try
        //        {
        //            testJiZhan();
        //        }
        //        catch (Exception e)
        //        {
        //            e.printStackTrace();
        //        }
//                        testpDCDC();
                                        testACDC();
        //                testGuanTongBatteryUpgradeStrategy();
    }

    void testpDCDC()
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                String fileName = "dcdc/TDK1000V75Z_HV01SV14_202000226.bin";
                String targetPath = Environment.getExternalStorageDirectory().getPath() + "/test_dc_files/" + fileName;
                File targetFile = new File(targetPath);
                if (targetFile != null && !targetFile.exists())
                {
                    AssetManager assets = getContext().getAssets();
                    try
                    {
                        FileUtil.copy(assets.open(fileName), targetPath);
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }

                }

                try
                {
                    Thread.sleep(10000);
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }

                DCDC_UpgradeStrategy pduSaiDeUpgradeStrategy = new DCDC_UpgradeStrategy((byte) 0x01);
                pduSaiDeUpgradeStrategy.setUpgradeFilePath(targetPath);
                //                pduSaiDeUpgradeStrategy.setUpgradeFilePath("/sdcard/Download/pdu/TDK1000V75Z_HV01SV13_20200116.bin");

                //                for (int address = 0x02; address <= 0x09; address++)
                //                {
                //                    DCDC_UpgradeStrategy obj = new DCDC_UpgradeStrategy((byte) address);
                //                    obj.setUpgradeFilePath("/sdcard/Download/pdu/TDK1000V75Z_HV01SV09_20200114.bin");
                //                    pduSaiDeUpgradeStrategy.addNext(obj);
                //                }

                pduSaiDeUpgradeStrategy.first().call(new Consumer<NodeStrategy>()
                {
                    @Override
                    public void accept(NodeStrategy nodeStrategy)
                    {
                        DispatcherManager.getInstance().dispatch(nodeStrategy);
                    }
                });
            }
        }).start();
    }

    void testACDC()
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    Thread.sleep(10000);
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }

                ACDC_UpgradeStrategy pduSaiDeUpgradeStrategy = new ACDC_UpgradeStrategy((byte) 0x51);
                pduSaiDeUpgradeStrategy.setUpgradeFilePath("/sdcard/Download/pdu/TC096K3000M1S_HV01SV02_20191225.bin");

//                for (int address = 0x52; address <= 0x52; address++)
//                {
//                    ACDC_UpgradeStrategy obj = new ACDC_UpgradeStrategy((byte) address);
//                    obj.setUpgradeFilePath("/sdcard/Download/pdu/TC096K3000M1S_HV01SV02_20191225.bin");
//                    pduSaiDeUpgradeStrategy.addNext(obj);
//                }

                pduSaiDeUpgradeStrategy.first().call(new Consumer<NodeStrategy>()
                {
                    @Override
                    public void accept(NodeStrategy nodeStrategy)
                    {
                        DispatcherManager.getInstance().dispatch(nodeStrategy);
                    }
                });
            }
        }).start();
    }

    void testpDU()
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    Thread.sleep(5000);
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }

                PduUpgradeStrategy pduUpgradeStrategy = new PduUpgradeStrategy();
                pduUpgradeStrategy.setPduUpgradeFilePath("/sdcard/Download/pdu/PDU_V106B01D00_20190717.bin");
                pduUpgradeStrategy.call(new Consumer<NodeStrategy>()
                {
                    @Override
                    public void accept(NodeStrategy nodeStrategy)
                    {
                        DispatcherManager.getInstance().dispatch(nodeStrategy);
                    }
                });
            }
        }).start();
    }

    public class SCell
    {
        public int MCC;
        public int MNC;
        public int LAC;
        public int CID;
    }

    private void testJiZhan() throws Exception
    {
        SCell cell = new SCell();

        /** 调用API获取基站信息 */
        TelephonyManager mTelNet = (TelephonyManager) getContext().getSystemService(Context.TELEPHONY_SERVICE);
        @SuppressLint("MissingPermission") GsmCellLocation location = (GsmCellLocation) mTelNet.getCellLocation();
        if (location == null)
            throw new Exception("获取基站信息失败");
        String operator = mTelNet.getNetworkOperator();
        int mcc = Integer.parseInt(operator.substring(0, 3));
        int mnc = Integer.parseInt(operator.substring(3));
        int cid = location.getCid();
        int lac = location.getLac();

        /** 将获得的数据放到结构体中 */
        cell.MCC = mcc;
        cell.MNC = mnc;
        cell.LAC = lac;
        cell.CID = cid;
        //http://www.cellid.cn/
        System.out.println("cell.MCC:" + cell.MCC);
        System.out.println("cell.MNC:" + cell.MNC);
        System.out.println("cell.LAC:" + cell.LAC);
        System.out.println("cell.CID:" + cell.CID);
    }


    private void testCheckCode()
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                System.out.println("校验码测试开始");
                try
                {
                    Thread.sleep(5000);
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
                Random random = new Random();

                CheckCodeStrategy checkCodeStrategy;
                int index = 0;
                int targetIndex;
                StringBuilder stringBuilder = new StringBuilder();
                while (true)
                {
                    if (monitorAdapter != null && monitorAdapter.getItemCount() > 0)
                    {
                        if (index > 0 && index % monitorAdapter.getItemCount() == 0)
                        {
                            index = 0;
                        }
                        targetIndex = index;
                        index++;

                        BatteryInfo batteryInfo = (BatteryInfo) monitorAdapter.getItem(targetIndex);
                        if (batteryInfo == null || TextUtils.isEmpty(batteryInfo.batteryIdInfo))
                        {
                            continue;
                        } else
                        {
                            checkCodeStrategy = new CheckCodeStrategy(batteryInfo.address);
                            stringBuilder.setLength(0);

                            for (int j = 0; j < 8; j++)
                            {
                                switch (random.nextInt(2) % 2)
                                {
                                    case 0:
                                        stringBuilder.append((char) (random.nextInt(26) + 'A'));
                                        break;
                                    default:
                                        stringBuilder.append(random.nextInt(10));
                                        break;
                                }
                            }

                            final String id = batteryInfo.batteryIdInfo;
                            final String code = stringBuilder.toString();
                            checkCodeStrategy.setCheckCode(code);
                            checkCodeStrategy.first().call(new Consumer<NodeStrategy>()
                            {
                                @Override
                                public void accept(NodeStrategy nodeStrategy)
                                {
                                    Logger.i("电池ID:" + id + ",写入:" + code);
                                    DispatcherManager.getInstance().dispatch(nodeStrategy);
                                }
                            });

                            //测试执行完等待5秒
                            try
                            {
                                Thread.sleep(5000);
                            }
                            catch (InterruptedException e)
                            {
                                e.printStackTrace();
                            }

                            batteryInfo = (BatteryInfo) monitorAdapter.getItem(targetIndex);
                            if (batteryInfo == null || TextUtils.isEmpty(batteryInfo.batteryIdInfo))
                            {
                                continue;
                            } else
                            {
                                Logger.i("电池ID:" + batteryInfo.batteryIdInfo + ",读取:" + batteryInfo.str_checkCode);
                            }
                        }
                    }
                }
            }
        }).start();
    }

    private void test()
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    Thread.sleep(5000);
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
                byte address = 0x05;
                ChargingStrategy chargingStrategy = new ChargingStrategy((byte) (address - 0x05 + 0x15));
                chargingStrategy.setBatterySpecification('M');
                chargingStrategy.addPrevious(new RelayOpenStrategy((byte) (address - 0x05 + 0x15)));
                chargingStrategy.first().call(new Consumer<NodeStrategy>()
                {
                    @Override
                    public void accept(NodeStrategy nodeStrategy)
                    {
                        DispatcherManager.getInstance().dispatch(nodeStrategy);
                    }
                });

            }
        }).start();
    }

    @Override
    protected int getLayoutID()
    {
        return R.layout.fragment_monitor;
    }

    public void setMachineVersion(@MachineVersion int version)
    {
        switch (version)
        {
            case MachineVersion.SC_3:
                monitorAdapter.setValues(4, 3, 10);
                break;
            case MachineVersion.SC_4:
            case MachineVersion.SC_5:
                monitorAdapter.setValues(3, 3, 10);
                break;
        }
    }

    private boolean isConnectedNet = false;

    private void checkLocked(BatteryInfo batteryInfo)
    {
        // TODO: 2019-10-18 判断是否电池正确放好
        if (batteryInfo.isLockedChange() && batteryInfo.isLocked())
        {
            System.out.println("新锁" + batteryInfo.address);
            if (isConnectedNet)
            {
                // TODO: 2019-10-25 换电-充电
                pushRod((byte) 0x0D);
            }
        }
    }

    private void pushRod(byte address)
    {
        if (appDialog.isShowing())
        {
            return;
        }
        appDialog.show(getActivity().getSupportFragmentManager(), "show");

        ChargingStrategy chargingStrategy = new ChargingStrategy((byte) (address - 0x05 + 0x15));
        chargingStrategy.setBatterySpecification('0');
        chargingStrategy.setOnChargingAction(new OnChargingAction()
        {
            @Override
            public void onChargingSuccessed(byte address)
            {
                System.out.println("设置充电成功" + address);
            }

            @Override
            public void onChargingFailed(byte address)
            {
                System.out.println("设置充电失败" + address);
            }
        });

        NodeStrategy pushNodeStrategy = null;
        PushRodStrategy pushRodStrategy = new PushRodStrategy(address);

        switch (MachineVersionConfig.getMachineVersion())
        {
            case MachineVersion.SC_3:
                pushNodeStrategy = pushRodStrategy;
                break;
            case MachineVersion.SC_4:
            case MachineVersion.SC_5:
                pushNodeStrategy = pushRodStrategy.addPrevious(new RelayCloseStrategy((byte) (address - 0x05 + 0x15)));
                break;
        }

        pushRodStrategy.addPrevious(chargingStrategy);
        pushRodStrategy.setOnPushAction(new OnPushAction()
        {
            @Override
            public void onPushed(boolean isSuccessed)
            {
                System.out.println(isSuccessed ? "推杆成功" : "推杆失败");
                appDialog.dismiss();
            }
        });

        if (pushNodeStrategy != null)
        {
            pushNodeStrategy.first()
                    .call(nodeStrategy -> DispatcherManager.getInstance().dispatch(nodeStrategy));
        }
    }

    private void testBatteryUpgradeStrategy()
    {
        // TODO: 2019-10-11 升级过程中其他控制操作必须暂停 
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                System.out.println("准备升级");
                try
                {
                    Thread.sleep(5000);
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
                System.out.println("开始升级");
                BatteryUpgradeStrategy batteryUpgradeStrategy = new NuoWanBatteryUpgradeStrategy((byte) 0x05, "/sdcard/Download" +
                        "/60V_V007_20191024.bin");
                //                batteryUpgradeStrategy.setOnUpgradeProgress(new OnUpgradeProgress()
                //                {
                //                    @Override
                //                    public void onUpgrade(BatteryUpgradeInfo batteryUpgradeInfo)
                //                    {
                //                        if (batteryViewModel != null)
                //                        {
                //                            batteryViewModel.batteryMonitorLiveData.postValue(batteryUpgradeInfo);
                //                        }
                //                    }
                //                });

                batteryUpgradeStrategy.addNext(new Action485((byte) 0x05))
                        .first()
                        .call(new Consumer<NodeStrategy>()
                        {
                            @Override
                            public void accept(NodeStrategy nodeStrategy)
                            {
                                DispatcherManager.getInstance().dispatch(nodeStrategy);
                            }
                        });

            }
        }).start();
    }

    private void testNulWanBatteryUpgradeStrategy()
    {
        // TODO: 2019-10-11 升级过程中其他控制操作必须暂停
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                System.out.println("准备升级");
                try
                {
                    Thread.sleep(5000);
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
                System.out.println("开始升级");
                BatteryUpgradeStrategy batteryUpgradeStrategy = new NuoWanBatteryUpgradeStrategy((byte) 0x07, "/sdcard/Download" +
                        "/MDBBB_1_255_7C79849A.bin");
                batteryUpgradeStrategy.setIdCodeAndBmsHardwareVersion("MDBBB", "1", "7C79849A");
                batteryUpgradeStrategy.addNext(new Action485((byte) 0x07))
                        .first()
                        .call(new Consumer<NodeStrategy>()
                        {
                            @Override
                            public void accept(NodeStrategy nodeStrategy)
                            {
                                DispatcherManager.getInstance().dispatch(nodeStrategy);
                            }
                        });

            }
        }).start();
    }

    private void testNulWanBatteryUpgradeStrategy_can()
    {
        // TODO: 2019-10-11 升级过程中其他控制操作必须暂停
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                String fileName = "battery/MDBBB_1_11_A3EECBEA.bin";
                String targetPath = Environment.getExternalStorageDirectory().getPath() + "/test_battery_files/" + fileName;
                File targetFile = new File(targetPath);
                if (targetFile != null && !targetFile.exists())
                {
                    AssetManager assets = getContext().getAssets();
                    try
                    {
                        FileUtil.copy(assets.open(fileName), targetPath);
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }

                }

                try
                {
                    Thread.sleep(10000);
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }

                System.out.println("准备升级");
                try
                {
                    Thread.sleep(10000);
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
                System.out.println("开始升级");
                BatteryUpgradeStrategy batteryUpgradeStrategy = new NuoWanBatteryUpgradeStrategy((byte) 0x01, targetPath);
                batteryUpgradeStrategy.setIdCodeAndBmsHardwareVersion("MDBBB", "112", "A3EECBEA");
                DispatcherManager.getInstance().dispatch(batteryUpgradeStrategy);
            }
        }).start();
    }

    @OnClick({R.id.tv_exitApp})
    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.tv_exitApp:
                getActivity().finish();
                break;
        }
    }

    private void testBoQiangBatteryUpgradeStrategy()
    {
        // TODO: 2019-10-11 升级过程中其他控制操作必须暂停
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                System.out.println("准备升级");
                try
                {
                    Thread.sleep(5000);
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
                System.out.println("开始升级");
                BatteryUpgradeStrategy batteryUpgradeStrategy = new BoQiangBatteryUpgradeStrategy((byte) 0x05, "/sdcard/Download" +
                        "/MBFFF_112_22_A193F5EB.bin");
                batteryUpgradeStrategy.setIdCodeAndBmsHardwareVersion("MBFFF", "112", "A193F5EB");
                batteryUpgradeStrategy.addNext(new Action485((byte) 0x05))
                        .first()
                        .call(new Consumer<NodeStrategy>()
                        {
                            @Override
                            public void accept(NodeStrategy nodeStrategy)
                            {
                                DispatcherManager.getInstance().dispatch(nodeStrategy);
                            }
                        });

            }
        }).start();
    }


    private void testChaoLiYuanUpgradeStrategy()
    {
        // TODO: 2019-10-11 升级过程中其他控制操作必须暂停
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                System.out.println("准备升级");
                try
                {
                    Thread.sleep(5000);
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
                System.out.println("开始升级");
                BatteryUpgradeStrategy batteryUpgradeStrategy = new ChaoLiYuanBatteryUpgradeStrategy((byte) 0x05, "/sdcard/Download" +
                        //                        "/60V_19_V006.bin");
                        "/NCDDD_20_160_3D855D1C.bin");
                batteryUpgradeStrategy.setIdCodeAndBmsHardwareVersion("NCDDD", "20", "3D855D1C");
                batteryUpgradeStrategy.addNext(new Action485((byte) 0x05))
                        .first()
                        .call(new Consumer<NodeStrategy>()
                        {
                            @Override
                            public void accept(NodeStrategy nodeStrategy)
                            {
                                DispatcherManager.getInstance().dispatch(nodeStrategy);
                            }
                        });

            }
        }).start();
    }

    private void testGuanTongBatteryUpgradeStrategy()
    {
        // TODO: 2019-10-11 升级过程中其他控制操作必须暂停
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                System.out.println("准备升级");
                try
                {
                    Thread.sleep(5000);
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
                System.out.println("开始升级");
                GuanTongBatteryUpgradeStrategy batteryUpgradeStrategy = new GuanTongBatteryUpgradeStrategy((byte) 0x05, "/sdcard/Download" +
                        "/MFKKK_12_2_DE9B115F.bin");
                batteryUpgradeStrategy.setIdCodeAndBmsHardwareVersion("MFKKK", "12", "DE9B115F");
                batteryUpgradeStrategy.addNext(new Action485((byte) 0x05))
                        .first()
                        .call(new Consumer<NodeStrategy>()
                        {
                            @Override
                            public void accept(NodeStrategy nodeStrategy)
                            {
                                DispatcherManager.getInstance().dispatch(nodeStrategy);
                            }
                        });

            }
        }).start();
    }
}
