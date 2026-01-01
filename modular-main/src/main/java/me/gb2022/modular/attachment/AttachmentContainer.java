package me.gb2022.modular.attachment;

import java.util.Map;
import java.util.Optional;

public interface AttachmentContainer<A> {
    void addAttachment(A attachment);

    void removeAttachment(Class<? extends A> type);

    <I extends A> I getAttachment(Class<I> type);

    Map<Class<? extends A>, A> getAttachments();

    default <I extends A> Optional<A> getAttachmentSafely(Class<I> type) {
        return Optional.ofNullable(getAttachment(type));
    }
}
