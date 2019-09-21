//
// Created by Lee Yang on 2019-09-18.
//

#include "com_android_canbus_api_Canbus.h"

#include <stdio.h>
#include <string.h>
#include <unistd.h>
#include <net/if.h>
#include <sys/ioctl.h>
#include <sys/socket.h>
#include <linux/can.h>
#include <linux/can/raw.h>
#include<termios.h>

int s;
char *device = "can0";
struct sockaddr_can addr;
struct ifreq ifr;


JNIEXPORT jobject JNICALL Java_com_android_1canbus_1api_Canbus_open(JNIEnv *env, jclass jz) {
    /* create a socket */
    s = socket(PF_CAN, SOCK_RAW, CAN_RAW);
    strcpy(ifr.ifr_name, device);
    /* determine the interface index */
    ioctl(s, SIOCGIFINDEX, &ifr);
    addr.can_family = AF_CAN;
    addr.can_ifindex = ifr.ifr_ifindex;
    /* bind the socket to a CAN interface */
    bind(s, (struct sockaddr *) &addr, sizeof(addr));

    jobject mFileDescriptor;
    jclass cFileDescriptor = (*env)->FindClass(env, "java/io/FileDescriptor");
    jmethodID iFileDescriptor = (*env)->GetMethodID(env, cFileDescriptor, "<init>", "()V");
    jfieldID descriptorID = (*env)->GetFieldID(env, cFileDescriptor, "descriptor", "I");
    mFileDescriptor = (*env)->NewObject(env, cFileDescriptor, iFileDescriptor);
    (*env)->SetIntField(env, mFileDescriptor, descriptorID, (jint) s);
    return mFileDescriptor;
    }

JNIEXPORT void JNICALL Java_com_android_1canbus_1api_Canbus_close(JNIEnv *env, jclass jz){
    close(s);
}
