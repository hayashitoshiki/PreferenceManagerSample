package com.myapp.preferencemanagersample.data.local.preferences

import android.content.Context
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
/**
 * Preference制御管理
 */
class PreferenceManager(val context: Context) {

    /**
     * Preferenceのキー
     */
    object Key {

        /**
         * Int型指定
         */
        enum class IntKey {
            INT1,
            INT2;
        }

        /**
         * List<Long>型指定
         */
        sealed class LongListKey : ListKey<Long>() {

            @Synchronized
            override fun update(preferenceManager: PreferenceManager, type: UpdateType, value: Long) {
                preferenceManager.updateList(this, value, type)
            }

            object Long1 : LongListKey()
            object Long2 : LongListKey()
        }

        /**
         * List型更新の際のBaseクラス
         *
         */
         abstract class ListKey<T> {
            /**
             * キー名
             */
            val name: String = this::class.java.simpleName

            /**
             * 更新処理
             *
             * Listの更新をする際に入り違いが起きないよう排他制御。
             * メソッドの先頭に@Synchronizedをつけ、
             * メソッドの中身にpreferenceManager.updateList(this, value, type)を記載
             * @param preferenceManager preference制御クラス
             */
             abstract fun update(preferenceManager: PreferenceManager, type: UpdateType, value: T)
        }
    }

    /**
     * 更新タイプ
     *
     */
    enum class UpdateType {
        /**
         * 追加
         *
         */
        ADD,

        /**
         * 削除
         *
         */
        REMOVE
    }

    /**
     * Int型格納
     *
     * @param key   キー
     * @param value 格納する値
     */
    fun setInt(
        key: Key.IntKey,
        value: Int
    ) {
        val preferences = context.getSharedPreferences("selfUpdateRoutine", Context.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.putInt(key.name, value)
        editor.apply()
    }

    /**
     * Int型取得
     *
     * @param key キー
     * @return キーに紐づくInt型オブジェクト
     */
    fun getInt(key: Key.IntKey): Int {
        val defaultValue = 0
        val preferences = context.getSharedPreferences("selfUpdateRoutine", Context.MODE_PRIVATE)
        return preferences.getInt(key.name, defaultValue)
    }

    /**
     * List<T>型値追加
     *
     * @param T リストの型
     * @param key キー
     * @param value 追加する値
     */
    fun <T> addFromList(key: Key.ListKey<T>, value: T) {
        key.update(this, UpdateType.ADD, value)
    }

    /**
     * List<T>型値削除
     *
     * @param T リストの型
     * @param key キー
     * @param value 削除する値
     */
    fun <T> removeFromList(key: Key.ListKey<T>, value: T) {
        key.update(this, UpdateType.REMOVE, value)
    }

    /**
     * List型取得
     *
     * @param T リストの中身の型
     * @param key 取得するリストのキー
     * @return 指定したリスト
     */
    inline fun <reified T> getList(key: Key.ListKey<T>): List<T> {
        val defaultValue = "[]"
        val result = context.getSharedPreferences("selfUpdateRoutine", Context.MODE_PRIVATE)
            .getString(key.name, defaultValue) ?: defaultValue
        return Json.decodeFromString(result)
    }

    /**
     * リスト型更新
     *
     * @param T リストの中身の型
     * @param key 取得するリストのキー
     * @param value 更新する値
     * @param updateType 更新内容
     */
    private inline fun <reified T> updateList(key: Key.ListKey<T>, value: T, updateType: UpdateType) {
        val list = getList(key).toMutableList()
        when(updateType) {
            UpdateType.ADD -> list.add(value)
            UpdateType.REMOVE -> list.remove(value)
        }
        val strValue = Json.encodeToString(list)
        context.getSharedPreferences("selfUpdateRoutine", Context.MODE_PRIVATE)
            .edit()
            .putString(key.name, strValue)
            .apply()
    }
}
