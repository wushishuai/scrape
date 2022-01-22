package com.bumptech.glide.load.data.mediastore;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import com.bumptech.glide.load.ImageHeaderParser;
import com.bumptech.glide.load.engine.bitmap_recycle.ArrayPool;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

/* loaded from: classes.dex */
class ThumbnailStreamOpener {
    private static final FileService DEFAULT_SERVICE = new FileService();
    private static final String TAG = "ThumbStreamOpener";
    private final ArrayPool byteArrayPool;
    private final ContentResolver contentResolver;
    private final List<ImageHeaderParser> parsers;
    private final ThumbnailQuery query;
    private final FileService service;

    /* JADX INFO: Access modifiers changed from: package-private */
    public ThumbnailStreamOpener(List<ImageHeaderParser> list, ThumbnailQuery thumbnailQuery, ArrayPool arrayPool, ContentResolver contentResolver) {
        this(list, DEFAULT_SERVICE, thumbnailQuery, arrayPool, contentResolver);
    }

    ThumbnailStreamOpener(List<ImageHeaderParser> list, FileService fileService, ThumbnailQuery thumbnailQuery, ArrayPool arrayPool, ContentResolver contentResolver) {
        this.service = fileService;
        this.query = thumbnailQuery;
        this.byteArrayPool = arrayPool;
        this.contentResolver = contentResolver;
        this.parsers = list;
    }

    /*  JADX ERROR: JadxRuntimeException in pass: SSATransform
        jadx.core.utils.exceptions.JadxRuntimeException: Not initialized variable reg: 0, insn: 0x0040: IF  (r0 I:??[int, boolean, OBJECT, ARRAY, byte, short, char]) == (0 ??[int, boolean, OBJECT, ARRAY, byte, short, char])  -> B:17:0x0045, block:B:15:0x0040
        	at jadx.core.dex.visitors.ssa.SSATransform.renameVarsInBlock(SSATransform.java:171)
        	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:143)
        	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:60)
        	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:41)
        */
    int getOrientation(
    /*  JADX ERROR: JadxRuntimeException in pass: SSATransform
        jadx.core.utils.exceptions.JadxRuntimeException: Not initialized variable reg: 0, insn: 0x0040: IF  (r0 I:??[int, boolean, OBJECT, ARRAY, byte, short, char]) == (0 ??[int, boolean, OBJECT, ARRAY, byte, short, char])  -> B:17:0x0045, block:B:15:0x0040
        	at jadx.core.dex.visitors.ssa.SSATransform.renameVarsInBlock(SSATransform.java:171)
        	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:143)
        	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:60)
        */
    /*  JADX ERROR: Method generation error
        jadx.core.utils.exceptions.JadxRuntimeException: Code variable not set in r6v0 ??
        	at jadx.core.dex.instructions.args.SSAVar.getCodeVar(SSAVar.java:233)
        	at jadx.core.codegen.MethodGen.addMethodArguments(MethodGen.java:209)
        	at jadx.core.codegen.MethodGen.addDefinition(MethodGen.java:162)
        	at jadx.core.codegen.ClassGen.addMethodCode(ClassGen.java:364)
        	at jadx.core.codegen.ClassGen.addMethod(ClassGen.java:304)
        	at jadx.core.codegen.ClassGen.lambda$addInnerClsAndMethods$2(ClassGen.java:270)
        	at java.util.stream.ForEachOps$ForEachOp$OfRef.accept(ForEachOps.java:184)
        	at java.util.ArrayList.forEach(ArrayList.java:1257)
        	at java.util.stream.SortedOps$RefSortingSink.end(SortedOps.java:390)
        	at java.util.stream.Sink$ChainedReference.end(Sink.java:258)
        */

    public InputStream open(Uri uri) throws FileNotFoundException {
        String path = getPath(uri);
        if (TextUtils.isEmpty(path)) {
            return null;
        }
        File file = this.service.get(path);
        if (!isValid(file)) {
            return null;
        }
        Uri fromFile = Uri.fromFile(file);
        try {
            return this.contentResolver.openInputStream(fromFile);
        } catch (NullPointerException e) {
            throw ((FileNotFoundException) new FileNotFoundException("NPE opening uri: " + uri + " -> " + fromFile).initCause(e));
        }
    }

    @Nullable
    private String getPath(@NonNull Uri uri) {
        Cursor query = this.query.query(uri);
        if (query != null) {
            try {
                if (query.moveToFirst()) {
                    return query.getString(0);
                }
            } finally {
                if (query != null) {
                    query.close();
                }
            }
        }
        if (query != null) {
            query.close();
        }
        return null;
    }

    private boolean isValid(File file) {
        return this.service.exists(file) && 0 < this.service.length(file);
    }
}
