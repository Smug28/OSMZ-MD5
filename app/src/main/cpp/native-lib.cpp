#include <jni.h>
#include <string>
#include "md5.h"
#include <algorithm>

const char inputChars[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l','m','n','o','p','q','r','s','t','u','v','w','x','y','z', 'A','B','C','D','E','F','G','H','I','J','K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'};
//const char* inputChars = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ\0";

std::string positions2String(int* positions, int size){
    std::string result = "";
    for (int i = 0; i < size; i++){
        result += inputChars[positions[i]];
    }
    return result;
}

extern "C"
JNIEXPORT jstring JNICALL
Java_cz_vacul_osmzmd5_MainActivity_calcHashCpp(JNIEnv *env, jclass type, jstring input_) {
    const char *input = env->GetStringUTFChars(input_, 0);

    jstring result = env->NewStringUTF(md5(input).c_str());

    env->ReleaseStringUTFChars(input_, input);

    return result;
}

extern "C"
JNIEXPORT jstring JNICALL
Java_cz_vacul_osmzmd5_MainActivity_crackMD5(JNIEnv *env, jclass type, jstring inputHash_, int inputSize) {
    const char *inputHash = env->GetStringUTFChars(inputHash_, 0);

    for (int length = 1; length <= inputSize; length++){
        int* positions = new int[length];
        for (int i = 0; i < length; i++)
            positions[i] = 0;

        std::string str = positions2String(positions, length);
        if (md5(str).compare(inputHash) == 0) {
            env->ReleaseStringUTFChars(inputHash_, inputHash);
            return env->NewStringUTF(str.c_str());
        }

        int i = length - 1;
        bool carry = false;
        while (i >= 0){
            if (i < 0 && carry)
                break;
            if (positions[i] >= 62) {
                positions[i] = 0;
                i--;
                carry = true;
                continue;
            }
            else {
                positions[i]++;
                if (positions[i] >= 62)
                    continue;
                else {
                    i = length - 1;
                    carry = false;
                }
            }

            str = positions2String(positions, length);
            if (md5(str).compare(inputHash) == 0) {
                env->ReleaseStringUTFChars(inputHash_, inputHash);
                return env->NewStringUTF(str.c_str());
            }
        }

        delete[] positions;
    }

    env->ReleaseStringUTFChars(inputHash_, inputHash);
    return NULL;
}

extern "C"
JNIEXPORT jstring JNICALL
Java_cz_vacul_osmzmd5_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}
