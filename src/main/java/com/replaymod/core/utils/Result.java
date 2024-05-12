package com.replaymod.core.utils;

import java.util.function.Consumer;
import java.util.function.Function;

public abstract class Result<Ok, Err> {
   public static <Ok, Err> Result.OkImpl<Ok, Err> ok(Ok value) {
      return new Result.OkImpl(value);
   }

   public static <Ok, Err> Result.ErrImpl<Ok, Err> err(Err value) {
      return new Result.ErrImpl(value);
   }

   public abstract boolean isOk();

   public abstract boolean isErr();

   public abstract Ok okOrNull();

   public abstract Err errOrNull();

   public abstract void ifOk(Consumer<Ok> var1);

   public abstract void ifErr(Consumer<Err> var1);

   public abstract Ok okOrElse(Function<Err, Ok> var1);

   public abstract Err errOrElse(Function<Ok, Err> var1);

   public abstract <T> Result<T, Err> mapOk(Function<Ok, T> var1);

   public abstract <T> Result<Ok, T> mapErr(Function<Err, T> var1);

   private static class OkImpl<Ok, Err> extends Result<Ok, Err> {
      private final Ok value;

      public OkImpl(Ok value) {
         this.value = value;
      }

      public boolean isOk() {
         return true;
      }

      public boolean isErr() {
         return false;
      }

      public Ok okOrNull() {
         return this.value;
      }

      public Err errOrNull() {
         return null;
      }

      public void ifOk(Consumer<Ok> consumer) {
         consumer.accept(this.value);
      }

      public void ifErr(Consumer<Err> consumer) {
      }

      public Ok okOrElse(Function<Err, Ok> orElse) {
         return this.value;
      }

      public Err errOrElse(Function<Ok, Err> orElse) {
         return orElse.apply(this.value);
      }

      public <V> Result<V, Err> mapOk(Function<Ok, V> func) {
         return ok(func.apply(this.value));
      }

      public <T> Result<Ok, T> mapErr(Function<Err, T> func) {
         return (Result<Ok, T>) this;
      }
   }

   private static class ErrImpl<Ok, Err> extends Result<Ok, Err> {
      private final Err value;

      public ErrImpl(Err value) {
         this.value = value;
      }

      public boolean isOk() {
         return false;
      }

      public boolean isErr() {
         return true;
      }

      public Ok okOrNull() {
         return null;
      }

      public Err errOrNull() {
         return this.value;
      }

      public void ifOk(Consumer<Ok> consumer) {
      }

      public void ifErr(Consumer<Err> consumer) {
         consumer.accept(this.value);
      }

      public Ok okOrElse(Function<Err, Ok> orElse) {
         return orElse.apply(this.value);
      }

      public Err errOrElse(Function<Ok, Err> orElse) {
         return this.value;
      }

      public <V> Result<V, Err> mapOk(Function<Ok, V> func) {
         return (Result<V, Err>) this;
      }

      public <T> Result<Ok, T> mapErr(Function<Err, T> func) {
         return err(func.apply(this.value));
      }
   }
}
