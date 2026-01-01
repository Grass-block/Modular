package me.gb2022.modular.attachment;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class SimpleAttachmentContainer<A> implements AttachmentContainer<A> {
    private final Map<Class<? extends A>, A> attachments = new ConcurrentHashMap<>();

    @Override
    public final void addAttachment(A attachment) {
        if (this.attachments.containsKey(attachment.getClass())) {
            throw new IllegalArgumentException("Attachment already exists: " + attachment.getClass());
        }

        this.attachments.put((Class<? extends A>) attachment.getClass(), attachment);
    }

    @Override
    public final void removeAttachment(Class<? extends A> type) {

        this.attachments.remove(type);
    }

    @Override
    public final <I extends A> I getAttachment(Class<I> type) {
        return type.cast(this.attachments.get(type));
    }

    @Override
    public final Map<Class<? extends A>, A> getAttachments() {
        return this.attachments;
    }

    @Override
    public final <I extends A> Optional<A> getAttachmentSafely(Class<I> type) {
        return AttachmentContainer.super.getAttachmentSafely(type);
    }
}
