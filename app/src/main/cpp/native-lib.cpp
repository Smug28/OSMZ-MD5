#include <jni.h>
#include <string>
#include "md5.h"

const char inputChars[] = {'A', 'B', 'C', 'D', 'E', 'F', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};

const char* positions2String(int* positions, int size){
    std::string result = "";
    for (int i = 0; i < size; i++){
        result += inputChars[positions[i]];
    }
    return result.c_str();
}

JNIEXPORT jstring JNICALL
Java_cz_vacul_osmzmd5_MainActivity_crackMD5(JNIEnv *env, jobject instance, jstring inputHash_) {
    const char *inputHash = env->GetStringUTFChars(inputHash_, 0);
    const int inputSize = 16;

    for (int length = 1; length < 10; length++){
        int* positions = new int[length];
        for (int i = 0; i < length; i++)
            positions[i] = 0;
        for (int pos = length - 1; pos >= 0; pos--) {
            for (int i = 0; i < inputSize; i++) {
                positions[pos] = i;
                const char* hash = genMD5(positions2String(positions, inputSize));
                if (strcmp(inputHash, hash) == 0) {
                    env->ReleaseStringUTFChars(inputHash_, inputHash);
                    return env->NewStringUTF(hash);
                }
            }
        }
    }

    env->ReleaseStringUTFChars(inputHash_, inputHash);

    return env->NewStringUTF("CUNTFUCK");
}

extern "C"
JNIEXPORT jstring JNICALL
Java_cz_vacul_osmzmd5_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}
