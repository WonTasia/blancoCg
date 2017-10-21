/*
 * blanco Framework
 * Copyright (C) 2004-2017 IGA Tosiki
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 */
/*
 * Copyright 2017 Toshiki Iga
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package blanco.cg.transformer.ruby;

import java.util.ArrayList;
import java.util.List;

import blanco.cg.BlancoCgSupportedLang;
import blanco.cg.util.BlancoCgSourceFileUtil;
import blanco.cg.valueobject.BlancoCgClass;
import blanco.cg.valueobject.BlancoCgInterface;
import blanco.cg.valueobject.BlancoCgSourceFile;
import blanco.commons.util.BlancoStringUtil;

/**
 * BlancoCgSourceFileをソースコードに展開します。
 * 
 * このクラスはblancoCgのバリューオブジェクトからソースコードを自動生成するトランスフォーマーの個別の展開機能です。
 * 
 * @author IGA Tosiki
 */
class BlancoCgSourceFileRubySourceExpander {
    /**
     * このクラスが処理対象とするプログラミング言語。
     */
    protected static final int TARGET_LANG = BlancoCgSupportedLang.RUBY;

    /**
     * 入力となるソースコード構造。
     */
    private BlancoCgSourceFile fCgSourceFile = null;

    /**
     * 中間的に利用するソースコードをあらわすList。java.lang.Stringがリストに格納されます。(BlancoCgLineではありません。
     * )
     * 
     * ここでは整形前ソースコードが中間的にたくわえられます。
     */
    private List<java.lang.String> fSourceLines = null;

    /**
     * SourceFileから整形前ソースコードリストを生成します。
     * 
     * @param argSourceFile
     *            ソースコードをあらわすバリューオブジェクト。
     * @return ソースコードに展開後のリスト。
     */
    public List<java.lang.String> transformSourceFile(
            final BlancoCgSourceFile argSourceFile) {
        // 確実にソース行のリストを初期化します。
        fSourceLines = new ArrayList<java.lang.String>();

        fCgSourceFile = argSourceFile;

        // ソースファイルのファイルヘッダーを出力処理します。
        expandSourceFileHeader();

        // パッケージ部分の生成。
        // if (BlancoStringUtil.null2Blank(fCgSourceFile.getPackage()).length()
        // > 0) {
        // fSourceLines.add("package " + fCgSourceFile.getPackage()
        // + BlancoCgLineUtil.getTerminator(TARGET_LANG));
        // fSourceLines.add("");
        // }
        //
        // if (fCgSourceFile.getImportList() == null) {
        // throw new IllegalArgumentException("importのリストにnullが与えられました。");
        // }
        //
        // // 処理の後半でインポート文を編成しなおしますが、その際に参照するアンカー文字列を追加しておきます。
        // BlancoCgImportJavaSourceExpander.insertAnchorString(fSourceLines);

        fSourceLines.add(""); // １行あける。

        // インタフェースの展開を実施します。
        if (fCgSourceFile.getInterfaceList() == null) {
            throw new IllegalArgumentException("インタフェースのリストにnullが与えられました。");
        }
        for (int index = 0; index < fCgSourceFile.getInterfaceList().size(); index++) {
            final BlancoCgInterface cgInterface = fCgSourceFile
                    .getInterfaceList().get(index);
            new BlancoCgInterfaceRubySourceExpander().transformInterface(
                    cgInterface, fCgSourceFile, fSourceLines);
        }

        // クラスの展開を実施します。
        if (fCgSourceFile.getClassList() == null) {
            throw new IllegalArgumentException("クラスのリストにnullが与えられました。");
        }
        for (int index = 0; index < fCgSourceFile.getClassList().size(); index++) {
            final BlancoCgClass cgClass = fCgSourceFile.getClassList().get(
                    index);

            new BlancoCgClassRubySourceExpander().transformClass(cgClass,
                    fCgSourceFile, fSourceLines);
        }

        // importの展開をします。
        // この処理が、クラス展開より後に実施されているのには意味があります。
        // クラス展開などを経て、初めてインポート文の一覧が確定するからです。
        // new BlancoCgImportJavaSourceExpander().transformImport(fCgSourceFile,
        // fSourceLines);

        return fSourceLines;
    }

    /**
     * ソースファイルのファイルヘッダーを出力処理します。
     */
    private void expandSourceFileHeader() {
        fSourceLines.add("#");
        if (BlancoStringUtil.null2Blank(fCgSourceFile.getDescription())
                .length() > 0) {
            fSourceLines.add("# " + fCgSourceFile.getDescription());
        } else {
            // 指定が無い場合にはデフォルトのコメントを利用します。
            for (String line : BlancoCgSourceFileUtil.getDefaultFileComment()) {
                fSourceLines.add("# " + line);
            }
        }

        // 言語ドキュメントの中間部を生成します。
        new BlancoCgLangDocRubySourceExpander().transformLangDocBody(
                fCgSourceFile.getLangDoc(), fSourceLines);

        fSourceLines.add("#");
    }
}
