var __getOwnPropNames = Object.getOwnPropertyNames;
var __commonJS = (cb, mod) => function __require() {
  return mod || (0, cb[__getOwnPropNames(cb)[0]])((mod = { exports: {} }).exports, mod), mod.exports;
};

// ../node_modules/@theia/electron/shared/electron/index.js
var require_electron = __commonJS({
  "../node_modules/@theia/electron/shared/electron/index.js"(exports2, module2) {
    module2.exports = require("electron");
  }
});

// ../node_modules/@theia/core/lib/common/array-utils.js
var require_array_utils = __commonJS({
  "../node_modules/@theia/core/lib/common/array-utils.js"(exports2) {
    "use strict";
    Object.defineProperty(exports2, "__esModule", { value: true });
    exports2.ArrayUtils = void 0;
    var ArrayUtils;
    (function(ArrayUtils2) {
      ArrayUtils2.TailImpl = {
        tail() {
          return this[this.length - 1];
        }
      };
      ArrayUtils2.HeadAndChildrenImpl = {
        head() {
          return this[0];
        },
        children() {
          return Object.assign(this.slice(1), ArrayUtils2.TailImpl);
        }
      };
      function asTail(array) {
        return Object.assign(array, ArrayUtils2.TailImpl);
      }
      ArrayUtils2.asTail = asTail;
      function asHeadAndTail(array) {
        return Object.assign(array, ArrayUtils2.HeadAndChildrenImpl, ArrayUtils2.TailImpl);
      }
      ArrayUtils2.asHeadAndTail = asHeadAndTail;
      let Sort;
      (function(Sort2) {
        Sort2[Sort2["LeftBeforeRight"] = -1] = "LeftBeforeRight";
        Sort2[Sort2["RightBeforeLeft"] = 1] = "RightBeforeLeft";
        Sort2[Sort2["Equal"] = 0] = "Equal";
      })(Sort = ArrayUtils2.Sort || (ArrayUtils2.Sort = {}));
      function binarySearch2(length, compareToKey) {
        let low = 0;
        let high = length - 1;
        while (low <= high) {
          const mid = (low + high) / 2 | 0;
          const comp = compareToKey(mid);
          if (comp < 0) {
            low = mid + 1;
          } else if (comp > 0) {
            high = mid - 1;
          } else {
            return mid;
          }
        }
        return -(low + 1);
      }
      ArrayUtils2.binarySearch2 = binarySearch2;
      function partition(array, filter) {
        const pass = [];
        const fail = [];
        array.forEach((e, idx, arr) => (filter(e, idx, arr) ? pass : fail).push(e));
        return [pass, fail];
      }
      ArrayUtils2.partition = partition;
      function coalesce(array) {
        return array.filter((e) => !!e);
      }
      ArrayUtils2.coalesce = coalesce;
      function groupBy(data, compare) {
        const result = [];
        let currentGroup = void 0;
        for (const element of data.slice(0).sort(compare)) {
          if (!currentGroup || compare(currentGroup[0], element) !== 0) {
            currentGroup = [element];
            result.push(currentGroup);
          } else {
            currentGroup.push(element);
          }
        }
        return result;
      }
      ArrayUtils2.groupBy = groupBy;
      function shallowEqual(left, right) {
        if (left.length !== right.length) {
          return false;
        }
        for (let i = 0; i < left.length; i++) {
          if (left[i] !== right[i]) {
            return false;
          }
        }
        return true;
      }
      ArrayUtils2.shallowEqual = shallowEqual;
      function startsWith(left, right) {
        if (right.length > left.length) {
          return false;
        }
        for (let i = 0; i < right.length; i++) {
          if (left[i] !== right[i]) {
            return false;
          }
        }
        return true;
      }
      ArrayUtils2.startsWith = startsWith;
      function equals(one, other, itemEquals = (a, b) => a === b) {
        if (one === other) {
          return true;
        }
        if (!one || !other) {
          return false;
        }
        if (one.length !== other.length) {
          return false;
        }
        for (let i = 0, len = one.length; i < len; i++) {
          if (!itemEquals(one[i], other[i])) {
            return false;
          }
        }
        return true;
      }
      ArrayUtils2.equals = equals;
      function findLast(array, predicate) {
        const idx = findLastIdx(array, predicate);
        if (idx === -1) {
          return void 0;
        }
        return array[idx];
      }
      ArrayUtils2.findLast = findLast;
      function findLastIdx(array, predicate, fromIndex = array.length - 1) {
        for (let i = fromIndex; i >= 0; i--) {
          const element = array[i];
          if (predicate(element)) {
            return i;
          }
        }
        return -1;
      }
      ArrayUtils2.findLastIdx = findLastIdx;
      function checkAdjacentItems(items, predicate) {
        for (let i = 0; i < items.length - 1; i++) {
          const a = items[i];
          const b = items[i + 1];
          if (!predicate(a, b)) {
            return false;
          }
        }
        return true;
      }
      ArrayUtils2.checkAdjacentItems = checkAdjacentItems;
    })(ArrayUtils || (exports2.ArrayUtils = ArrayUtils = {}));
  }
});

// ../node_modules/@theia/core/lib/common/prioritizeable.js
var require_prioritizeable = __commonJS({
  "../node_modules/@theia/core/lib/common/prioritizeable.js"(exports2) {
    "use strict";
    Object.defineProperty(exports2, "__esModule", { value: true });
    exports2.Prioritizeable = void 0;
    var Prioritizeable;
    (function(Prioritizeable2) {
      async function toPrioritizeable(rawValue, getPriority) {
        if (rawValue instanceof Array) {
          return Promise.all(rawValue.map((v) => toPrioritizeable(v, getPriority)));
        }
        const value = await rawValue;
        const priority = await getPriority(value);
        return { priority, value };
      }
      Prioritizeable2.toPrioritizeable = toPrioritizeable;
      function toPrioritizeableSync(rawValue, getPriority) {
        return rawValue.map((v) => ({
          value: v,
          priority: getPriority(v)
        }));
      }
      Prioritizeable2.toPrioritizeableSync = toPrioritizeableSync;
      function prioritizeAllSync(values, getPriority) {
        const prioritizeable = toPrioritizeableSync(values, getPriority);
        return prioritizeable.filter(isValid).sort(compare);
      }
      Prioritizeable2.prioritizeAllSync = prioritizeAllSync;
      async function prioritizeAll(values, getPriority) {
        const prioritizeable = await toPrioritizeable(values, getPriority);
        return prioritizeable.filter(isValid).sort(compare);
      }
      Prioritizeable2.prioritizeAll = prioritizeAll;
      function isValid(p) {
        return p.priority > 0;
      }
      Prioritizeable2.isValid = isValid;
      function compare(p, p2) {
        return p2.priority - p.priority;
      }
      Prioritizeable2.compare = compare;
    })(Prioritizeable || (exports2.Prioritizeable = Prioritizeable = {}));
  }
});

// ../node_modules/@theia/core/lib/common/types.js
var require_types = __commonJS({
  "../node_modules/@theia/core/lib/common/types.js"(exports2) {
    "use strict";
    Object.defineProperty(exports2, "__esModule", { value: true });
    exports2.MapUtils = exports2.Prioritizeable = exports2.ArrayUtils = void 0;
    exports2.isBoolean = isBoolean;
    exports2.isString = isString;
    exports2.isNumber = isNumber;
    exports2.isError = isError;
    exports2.isErrorLike = isErrorLike;
    exports2.isFunction = isFunction;
    exports2.isEmptyObject = isEmptyObject;
    exports2.isObject = isObject;
    exports2.isUndefined = isUndefined;
    exports2.isArray = isArray;
    exports2.isStringArray = isStringArray;
    exports2.nullToUndefined = nullToUndefined;
    exports2.unreachable = unreachable;
    exports2.isDefined = isDefined;
    exports2.isUndefinedOrNull = isUndefinedOrNull;
    var array_utils_1 = require_array_utils();
    Object.defineProperty(exports2, "ArrayUtils", { enumerable: true, get: function() {
      return array_utils_1.ArrayUtils;
    } });
    var prioritizeable_1 = require_prioritizeable();
    Object.defineProperty(exports2, "Prioritizeable", { enumerable: true, get: function() {
      return prioritizeable_1.Prioritizeable;
    } });
    function isBoolean(value) {
      return value === true || value === false;
    }
    function isString(value) {
      return typeof value === "string" || value instanceof String;
    }
    function isNumber(value) {
      return typeof value === "number" || value instanceof Number;
    }
    function isError(value) {
      return value instanceof Error;
    }
    function isErrorLike(value) {
      return isObject(value) && isString(value.name) && isString(value.message) && (isUndefined(value.stack) || isString(value.stack));
    }
    function isFunction(value) {
      return typeof value === "function";
    }
    function isEmptyObject(obj) {
      if (!isObject(obj)) {
        return false;
      }
      for (const key in obj) {
        if (Object.prototype.hasOwnProperty.call(obj, key)) {
          return false;
        }
      }
      return true;
    }
    function isObject(value) {
      return typeof value === "object" && value !== null;
    }
    function isUndefined(value) {
      return typeof value === "undefined";
    }
    function isArray(value, every, thisArg) {
      return Array.isArray(value) && (!isFunction(every) || value.every(every, thisArg));
    }
    function isStringArray(value) {
      return isArray(value, isString);
    }
    function nullToUndefined(nullable) {
      const undefinable = { ...nullable };
      for (const key in nullable) {
        if (nullable[key] === null && Object.prototype.hasOwnProperty.call(nullable, key)) {
          undefinable[key] = void 0;
        }
      }
      return undefinable;
    }
    function unreachable(_never, message = "unhandled case") {
      throw new Error(message);
    }
    function isDefined(arg) {
      return !isUndefinedOrNull(arg);
    }
    function isUndefinedOrNull(obj) {
      return isUndefined(obj) || obj === null;
    }
    var MapUtils;
    (function(MapUtils2) {
      function addOrInsertWith(container, key, ...values) {
        const existing = container.get(key);
        if (existing) {
          existing.push(...values);
        } else {
          container.set(key, values);
        }
      }
      MapUtils2.addOrInsertWith = addOrInsertWith;
    })(MapUtils || (exports2.MapUtils = MapUtils = {}));
  }
});

// ../node_modules/@theia/core/lib/common/cancellation.js
var require_cancellation = __commonJS({
  "../node_modules/@theia/core/lib/common/cancellation.js"(exports2) {
    "use strict";
    Object.defineProperty(exports2, "__esModule", { value: true });
    exports2.CancellationTokenSource = exports2.CancellationError = exports2.CancellationToken = void 0;
    exports2.cancelled = cancelled;
    exports2.isCancelled = isCancelled;
    exports2.checkCancelled = checkCancelled;
    var event_1 = require_event();
    var types_1 = require_types();
    var shortcutEvent = Object.freeze(Object.assign(function(callback, context) {
      const handle = setTimeout(callback.bind(context), 0);
      return { dispose() {
        clearTimeout(handle);
      } };
    }, {
      get maxListeners() {
        return 0;
      },
      set maxListeners(maxListeners) {
      }
    }));
    var CancellationToken;
    (function(CancellationToken2) {
      CancellationToken2.None = Object.freeze({
        isCancellationRequested: false,
        onCancellationRequested: event_1.Event.None
      });
      CancellationToken2.Cancelled = Object.freeze({
        isCancellationRequested: true,
        onCancellationRequested: shortcutEvent
      });
      function is(value) {
        return (0, types_1.isObject)(value) && (value === CancellationToken2.None || value === CancellationToken2.Cancelled || (0, types_1.isBoolean)(value.isCancellationRequested) && !!value.onCancellationRequested);
      }
      CancellationToken2.is = is;
    })(CancellationToken || (exports2.CancellationToken = CancellationToken = {}));
    var CancellationError = class extends Error {
      constructor() {
        super("Canceled");
        this.name = this.message;
      }
    };
    exports2.CancellationError = CancellationError;
    var MutableToken = class {
      constructor() {
        this._isCancelled = false;
      }
      cancel() {
        if (!this._isCancelled) {
          this._isCancelled = true;
          if (this._emitter) {
            this._emitter.fire(void 0);
            this._emitter = void 0;
          }
        }
      }
      get isCancellationRequested() {
        return this._isCancelled;
      }
      get onCancellationRequested() {
        if (this._isCancelled) {
          return shortcutEvent;
        }
        if (!this._emitter) {
          this._emitter = new event_1.Emitter();
        }
        return this._emitter.event;
      }
      dispose() {
        if (this._emitter) {
          this._emitter.dispose();
          this._emitter = void 0;
        }
      }
    };
    var CancellationTokenSource = class {
      constructor(parent) {
        this._parentListener = void 0;
        this._parentListener = parent && parent.onCancellationRequested(this.cancel, this);
      }
      get token() {
        if (!this._token) {
          this._token = new MutableToken();
        }
        return this._token;
      }
      cancel() {
        if (!this._token) {
          this._token = CancellationToken.Cancelled;
        } else if (this._token !== CancellationToken.Cancelled) {
          this._token.cancel();
        }
      }
      dispose() {
        this.cancel();
        this._parentListener?.dispose();
        if (!this._token) {
          this._token = CancellationToken.None;
        } else if (this._token instanceof MutableToken) {
          this._token.dispose();
        }
      }
    };
    exports2.CancellationTokenSource = CancellationTokenSource;
    var cancelledMessage = "Cancelled";
    function cancelled() {
      return new Error(cancelledMessage);
    }
    function isCancelled(err) {
      return !!err && err.message === cancelledMessage;
    }
    function checkCancelled(token) {
      if (!!token && token.isCancellationRequested) {
        throw cancelled();
      }
    }
  }
});

// ../node_modules/@theia/core/lib/common/event.js
var require_event = __commonJS({
  "../node_modules/@theia/core/lib/common/event.js"(exports2) {
    "use strict";
    Object.defineProperty(exports2, "__esModule", { value: true });
    exports2.QueueableEmitter = exports2.AsyncEmitter = exports2.WaitUntilEvent = exports2.Emitter = exports2.Event = void 0;
    var disposable_1 = require_disposable();
    var Event;
    (function(Event2) {
      const _disposable = { dispose() {
      } };
      function getMaxListeners(event) {
        const { maxListeners } = event;
        return typeof maxListeners === "number" ? maxListeners : 0;
      }
      Event2.getMaxListeners = getMaxListeners;
      function setMaxListeners(event, maxListeners) {
        if (typeof event.maxListeners === "number") {
          return event.maxListeners = maxListeners;
        }
        return maxListeners;
      }
      Event2.setMaxListeners = setMaxListeners;
      function addMaxListeners(event, add) {
        if (typeof event.maxListeners === "number") {
          return event.maxListeners += add;
        }
        return add;
      }
      Event2.addMaxListeners = addMaxListeners;
      Event2.None = Object.assign(function() {
        return _disposable;
      }, {
        get maxListeners() {
          return 0;
        },
        set maxListeners(maxListeners) {
        }
      });
      function once(event) {
        return (listener, thisArgs = void 0, disposables) => {
          let didFire = false;
          let result = void 0;
          result = event((e) => {
            if (didFire) {
              return;
            } else if (result) {
              result.dispose();
            } else {
              didFire = true;
            }
            return listener.call(thisArgs, e);
          }, void 0, disposables);
          if (didFire) {
            result.dispose();
          }
          return result;
        };
      }
      Event2.once = once;
      function toPromise(event) {
        return new Promise((resolve) => once(event)(resolve));
      }
      Event2.toPromise = toPromise;
      function filter(event, predicate) {
        return (listener, thisArg, disposables) => event((e) => predicate(e) && listener.call(thisArg, e), void 0, disposables);
      }
      Event2.filter = filter;
      function map(event, mapFunc) {
        return Object.assign((listener, thisArgs, disposables) => event((i) => listener.call(thisArgs, mapFunc(i)), void 0, disposables), {
          get maxListeners() {
            return 0;
          },
          set maxListeners(maxListeners) {
          }
        });
      }
      Event2.map = map;
      function any(...events) {
        return (listener, thisArgs = void 0, disposables) => new disposable_1.DisposableCollection(...events.map((event) => event((e) => listener.call(thisArgs, e), void 0, disposables)));
      }
      Event2.any = any;
    })(Event || (exports2.Event = Event = {}));
    var CallbackList = class {
      constructor(errorHandling = "log") {
        this.errorHandling = errorHandling;
      }
      get length() {
        return this._callbacks && this._callbacks.length || 0;
      }
      add(callback, context = void 0, bucket) {
        if (!this._callbacks) {
          this._callbacks = [];
          this._contexts = [];
        }
        this._callbacks.push(callback);
        this._contexts.push(context);
        if (Array.isArray(bucket)) {
          bucket.push({ dispose: () => this.remove(callback, context) });
        }
      }
      remove(callback, context = void 0) {
        if (!this._callbacks) {
          return;
        }
        let foundCallbackWithDifferentContext = false;
        for (let i = 0; i < this._callbacks.length; i++) {
          if (this._callbacks[i] === callback) {
            if (this._contexts[i] === context) {
              this._callbacks.splice(i, 1);
              this._contexts.splice(i, 1);
              return;
            } else {
              foundCallbackWithDifferentContext = true;
            }
          }
        }
        if (foundCallbackWithDifferentContext) {
          throw new Error("When adding a listener with a context, you should remove it with the same context");
        }
      }
      // tslint:disable-next-line:typedef
      [Symbol.iterator]() {
        if (!this._callbacks) {
          return [][Symbol.iterator]();
        }
        const callbacks = this._callbacks.slice(0);
        const contexts = this._contexts.slice(0);
        return callbacks.map((callback, i) => (...args) => callback.apply(contexts[i], args))[Symbol.iterator]();
      }
      invoke(...args) {
        const ret = [];
        const errors = [];
        for (const callback of this) {
          try {
            ret.push(callback(...args));
          } catch (e) {
            if (this.errorHandling === "propagate") {
              errors.push(e);
            } else if (typeof this.errorHandling === "function") {
              this.errorHandling(e);
            } else {
              console.error(e);
            }
          }
        }
        if (errors.length === 1) {
          throw errors[0];
        } else if (errors.length > 1) {
          throw new AggregateError(errors, "Multiple event listeners failed");
        }
        return ret;
      }
      isEmpty() {
        return !this._callbacks || this._callbacks.length === 0;
      }
      dispose() {
        this._callbacks = void 0;
        this._contexts = void 0;
      }
    };
    var Emitter = class _Emitter {
      static {
        this.LEAK_WARNING_THRESHHOLD = 175;
      }
      static {
        this._noop = function() {
        };
      }
      constructor(_options) {
        this._options = _options;
        this._disposed = false;
        this._leakWarnCountdown = 0;
      }
      /**
       * For the public to allow to subscribe
       * to events from this Emitter
       */
      get event() {
        if (!this._event) {
          this._event = Object.assign((listener, thisArgs, disposables) => {
            if (!this._callbacks) {
              this._callbacks = new CallbackList(this._options?.errorHandling);
            }
            if (this._options && this._options.onFirstListenerAdd && this._callbacks.isEmpty()) {
              this._options.onFirstListenerAdd(this);
            }
            this._callbacks.add(listener, thisArgs);
            const removeMaxListenersCheck = this.checkMaxListeners(Event.getMaxListeners(this._event));
            const result = {
              dispose: () => {
                if (removeMaxListenersCheck) {
                  removeMaxListenersCheck();
                }
                result.dispose = _Emitter._noop;
                if (!this._disposed) {
                  this._callbacks.remove(listener, thisArgs);
                  result.dispose = _Emitter._noop;
                  if (this._options && this._options.onLastListenerRemove && this._callbacks.isEmpty()) {
                    this._options.onLastListenerRemove(this);
                  }
                }
              }
            };
            if (disposable_1.DisposableGroup.canPush(disposables)) {
              disposables.push(result);
            } else if (disposable_1.DisposableGroup.canAdd(disposables)) {
              disposables.add(result);
            }
            return result;
          }, {
            maxListeners: _Emitter.LEAK_WARNING_THRESHHOLD
          });
        }
        return this._event;
      }
      checkMaxListeners(maxListeners) {
        if (maxListeners === 0 || !this._callbacks) {
          return void 0;
        }
        const listenerCount = this._callbacks.length;
        if (listenerCount <= maxListeners) {
          return void 0;
        }
        const popStack = this.pushLeakingStack();
        this._leakWarnCountdown -= 1;
        if (this._leakWarnCountdown <= 0) {
          this._leakWarnCountdown = maxListeners * 0.5;
          let topStack;
          let topCount = 0;
          this._leakingStacks.forEach((stackCount, stack) => {
            if (!topStack || topCount < stackCount) {
              topStack = stack;
              topCount = stackCount;
            }
          });
          console.warn(`Possible Emitter memory leak detected. ${listenerCount} listeners added. Use event.maxListeners to increase the limit (${maxListeners}). MOST frequent listener (${topCount}):`);
          console.warn(topStack);
        }
        return popStack;
      }
      pushLeakingStack() {
        if (!this._leakingStacks) {
          this._leakingStacks = /* @__PURE__ */ new Map();
        }
        const stack = new Error().stack.split("\n").slice(3).join("\n");
        const count = this._leakingStacks.get(stack) || 0;
        this._leakingStacks.set(stack, count + 1);
        return () => this.popLeakingStack(stack);
      }
      popLeakingStack(stack) {
        if (!this._leakingStacks) {
          return;
        }
        const count = this._leakingStacks.get(stack) || 0;
        this._leakingStacks.set(stack, count - 1);
      }
      /**
       * To be kept private to fire an event to
       * subscribers
       */
      fire(event) {
        if (this._callbacks) {
          return this._callbacks.invoke(event);
        }
      }
      /**
       * Process each listener one by one.
       * Return `false` to stop iterating over the listeners, `true` to continue.
       */
      async sequence(processor) {
        if (this._callbacks) {
          for (const listener of this._callbacks) {
            if (!await processor(listener)) {
              break;
            }
          }
        }
      }
      dispose() {
        if (this._leakingStacks) {
          this._leakingStacks.clear();
          this._leakingStacks = void 0;
        }
        if (this._callbacks) {
          this._callbacks.dispose();
          this._callbacks = void 0;
        }
        this._disposed = true;
      }
    };
    exports2.Emitter = Emitter;
    var WaitUntilEvent;
    (function(WaitUntilEvent2) {
      async function fire(emitter, event, timeout, token = cancellation_1.CancellationToken.None) {
        const waitables = [];
        const asyncEvent = Object.assign(event, {
          token,
          waitUntil: (thenable) => {
            if (Object.isFrozen(waitables)) {
              throw new Error("waitUntil cannot be called asynchronously.");
            }
            waitables.push(thenable);
          }
        });
        try {
          emitter.fire(asyncEvent);
          Object.freeze(waitables);
        } finally {
          delete asyncEvent["waitUntil"];
        }
        if (!waitables.length) {
          return;
        }
        if (timeout !== void 0) {
          await Promise.race([Promise.all(waitables), new Promise((resolve) => setTimeout(resolve, timeout))]);
        } else {
          await Promise.all(waitables);
        }
      }
      WaitUntilEvent2.fire = fire;
    })(WaitUntilEvent || (exports2.WaitUntilEvent = WaitUntilEvent = {}));
    var cancellation_1 = require_cancellation();
    var AsyncEmitter = class extends Emitter {
      /**
       * Fire listeners async one after another.
       */
      fire(event, token = cancellation_1.CancellationToken.None, promiseJoin) {
        const callbacks = this._callbacks;
        if (!callbacks) {
          return Promise.resolve();
        }
        const listeners = [...callbacks];
        if (this.deliveryQueue) {
          return this.deliveryQueue = this.deliveryQueue.then(() => this.deliver(listeners, event, token, promiseJoin));
        }
        return this.deliveryQueue = this.deliver(listeners, event, token, promiseJoin);
      }
      async deliver(listeners, event, token, promiseJoin) {
        for (const listener of listeners) {
          if (token.isCancellationRequested) {
            return;
          }
          const waitables = [];
          const asyncEvent = Object.assign(event, {
            token,
            waitUntil: (thenable) => {
              if (Object.isFrozen(waitables)) {
                throw new Error("waitUntil cannot be called asynchronously.");
              }
              if (promiseJoin) {
                thenable = promiseJoin(thenable, listener);
              }
              waitables.push(thenable);
            }
          });
          try {
            listener(event);
            Object.freeze(waitables);
          } catch (e) {
            console.error(e);
          } finally {
            delete asyncEvent["waitUntil"];
          }
          if (!waitables.length) {
            continue;
          }
          try {
            await Promise.all(waitables);
          } catch (e) {
            console.error(e);
          }
        }
      }
    };
    exports2.AsyncEmitter = AsyncEmitter;
    var QueueableEmitter = class extends Emitter {
      queue(...arg) {
        if (!this.currentQueue) {
          this.currentQueue = [];
        }
        this.currentQueue.push(...arg);
      }
      fire() {
        super.fire(this.currentQueue || []);
        this.currentQueue = void 0;
      }
    };
    exports2.QueueableEmitter = QueueableEmitter;
  }
});

// ../node_modules/@theia/core/lib/common/disposable.js
var require_disposable = __commonJS({
  "../node_modules/@theia/core/lib/common/disposable.js"(exports2) {
    "use strict";
    Object.defineProperty(exports2, "__esModule", { value: true });
    exports2.DisposableWrapper = exports2.DisposableGroup = exports2.DisposableCollection = exports2.Disposable = void 0;
    exports2.disposableTimeout = disposableTimeout;
    var event_1 = require_event();
    var types_1 = require_types();
    var Disposable;
    (function(Disposable2) {
      function is(arg) {
        return (0, types_1.isObject)(arg) && (0, types_1.isFunction)(arg.dispose);
      }
      Disposable2.is = is;
      function create(func) {
        return { dispose: func };
      }
      Disposable2.create = create;
    })(Disposable || (exports2.Disposable = Disposable = {}));
    Object.defineProperty(Disposable, "NULL", {
      configurable: false,
      enumerable: true,
      get() {
        return { dispose: () => {
        } };
      }
    });
    var DisposableCollection = class {
      constructor(...toDispose) {
        this.disposables = [];
        this.onDisposeEmitter = new event_1.Emitter();
        this.disposingElements = false;
        toDispose.forEach((d) => this.push(d));
      }
      /**
       * This event is fired only once
       * on first dispose of not empty collection.
       */
      get onDispose() {
        return this.onDisposeEmitter.event;
      }
      checkDisposed() {
        if (this.disposed && !this.disposingElements) {
          this.onDisposeEmitter.fire(void 0);
          this.onDisposeEmitter.dispose();
        }
      }
      get disposed() {
        return this.disposables.length === 0;
      }
      dispose() {
        if (this.disposed || this.disposingElements) {
          return;
        }
        this.disposingElements = true;
        while (!this.disposed) {
          try {
            this.disposables.pop().dispose();
          } catch (e) {
            console.error(e);
          }
        }
        this.disposingElements = false;
        this.checkDisposed();
      }
      push(disposable) {
        const disposables = this.disposables;
        disposables.push(disposable);
        const originalDispose = disposable.dispose.bind(disposable);
        const toRemove = Disposable.create(() => {
          const index = disposables.indexOf(disposable);
          if (index !== -1) {
            disposables.splice(index, 1);
          }
          this.checkDisposed();
        });
        disposable.dispose = () => {
          toRemove.dispose();
          disposable.dispose = originalDispose;
          originalDispose();
        };
        return toRemove;
      }
      pushAll(disposables) {
        return disposables.map((disposable) => this.push(disposable));
      }
    };
    exports2.DisposableCollection = DisposableCollection;
    var DisposableGroup;
    (function(DisposableGroup2) {
      function canPush(candidate) {
        return Boolean(candidate && candidate.push);
      }
      DisposableGroup2.canPush = canPush;
      function canAdd(candidate) {
        return Boolean(candidate && candidate.add);
      }
      DisposableGroup2.canAdd = canAdd;
    })(DisposableGroup || (exports2.DisposableGroup = DisposableGroup = {}));
    function disposableTimeout(...args) {
      const handle = setTimeout(...args);
      return { dispose: () => clearTimeout(handle) };
    }
    var DisposableWrapper = class {
      constructor() {
        this.disposed = false;
        this.disposable = void 0;
      }
      set(disposable) {
        if (this.disposed) {
          disposable.dispose();
        } else {
          this.disposable = disposable;
        }
      }
      dispose() {
        this.disposed = true;
        if (this.disposable) {
          this.disposable.dispose();
          this.disposable = void 0;
        }
      }
    };
    exports2.DisposableWrapper = DisposableWrapper;
  }
});

// ../node_modules/@theia/core/lib/electron-common/electron-api.js
var require_electron_api = __commonJS({
  "../node_modules/@theia/core/lib/electron-common/electron-api.js"(exports2) {
    "use strict";
    Object.defineProperty(exports2, "__esModule", { value: true });
    exports2.CHANNEL_IPC_CONNECTION = exports2.CHANNEL_KEYBOARD_LAYOUT_CHANGED = exports2.CHANNEL_WRITE_CLIPBOARD = exports2.CHANNEL_READ_CLIPBOARD = exports2.CHANNEL_APP_STATE_CHANGED = exports2.CHANNEL_RESTART = exports2.CHANNEL_REQUEST_RELOAD = exports2.CHANNEL_REQUEST_CLOSE = exports2.CHANNEL_REQUEST_SECONDARY_CLOSE = exports2.CHANNEL_TOGGLE_FULL_SCREEN = exports2.CHANNEL_IS_FULL_SCREEN = exports2.CHANNEL_IS_FULL_SCREENABLE = exports2.CHANNEL_SET_ZOOM_LEVEL = exports2.CHANNEL_GET_ZOOM_LEVEL = exports2.CHANNEL_OPEN_DEVTOOLS_FOR_WINDOW = exports2.CHANNEL_TOGGLE_DEVTOOLS = exports2.CHANNEL_ON_WINDOW_EVENT = exports2.CHANNEL_UNMAXIMIZE = exports2.CHANNEL_OPEN_URL = exports2.CHANNEL_ABOUT_TO_CLOSE = exports2.CHANNEL_IS_MAXIMIZED = exports2.CHANNEL_MAXIMIZE = exports2.CHANNEL_MINIMIZE = exports2.CHANNEL_CLOSE = exports2.CHANNEL_SET_THEME = exports2.CHANNEL_SET_BACKGROUND_COLOR = exports2.CHANNEL_SET_TITLE_STYLE = exports2.CHANNEL_GET_TITLE_STYLE_AT_STARTUP = exports2.CHANNEL_ATTACH_SECURITY_TOKEN = exports2.CHANNEL_OPEN_WITH_SYSTEM_APP = exports2.CHANNEL_SHOW_ITEM_IN_FOLDER = exports2.CHANNEL_SHOW_SAVE = exports2.CHANNEL_SHOW_OPEN = exports2.CHANNEL_FOCUS_WINDOW = exports2.CHANNEL_GET_SECURITY_TOKEN = exports2.CHANNEL_CLOSE_POPUP = exports2.CHANNEL_ON_CLOSE_POPUP = exports2.CHANNEL_OPEN_POPUP = exports2.CHANNEL_INVOKE_MENU = exports2.CHANNEL_SET_MENU_BAR_VISIBLE = exports2.CHANNEL_SET_MENU = exports2.CHANNEL_WC_METADATA = void 0;
    exports2.CHANNEL_WC_METADATA = "WebContentMetadata";
    exports2.CHANNEL_SET_MENU = "SetMenu";
    exports2.CHANNEL_SET_MENU_BAR_VISIBLE = "SetMenuBarVisible";
    exports2.CHANNEL_INVOKE_MENU = "InvokeMenu";
    exports2.CHANNEL_OPEN_POPUP = "OpenPopup";
    exports2.CHANNEL_ON_CLOSE_POPUP = "OnClosePopup";
    exports2.CHANNEL_CLOSE_POPUP = "ClosePopup";
    exports2.CHANNEL_GET_SECURITY_TOKEN = "GetSecurityToken";
    exports2.CHANNEL_FOCUS_WINDOW = "FocusWindow";
    exports2.CHANNEL_SHOW_OPEN = "ShowOpenDialog";
    exports2.CHANNEL_SHOW_SAVE = "ShowSaveDialog";
    exports2.CHANNEL_SHOW_ITEM_IN_FOLDER = "ShowItemInFolder";
    exports2.CHANNEL_OPEN_WITH_SYSTEM_APP = "OpenWithSystemApp";
    exports2.CHANNEL_ATTACH_SECURITY_TOKEN = "AttachSecurityToken";
    exports2.CHANNEL_GET_TITLE_STYLE_AT_STARTUP = "GetTitleStyleAtStartup";
    exports2.CHANNEL_SET_TITLE_STYLE = "SetTitleStyle";
    exports2.CHANNEL_SET_BACKGROUND_COLOR = "SetBackgroundColor";
    exports2.CHANNEL_SET_THEME = "SetTheme";
    exports2.CHANNEL_CLOSE = "Close";
    exports2.CHANNEL_MINIMIZE = "Minimize";
    exports2.CHANNEL_MAXIMIZE = "Maximize";
    exports2.CHANNEL_IS_MAXIMIZED = "IsMaximized";
    exports2.CHANNEL_ABOUT_TO_CLOSE = "AboutToClose";
    exports2.CHANNEL_OPEN_URL = "OpenUrl";
    exports2.CHANNEL_UNMAXIMIZE = "UnMaximize";
    exports2.CHANNEL_ON_WINDOW_EVENT = "OnWindowEvent";
    exports2.CHANNEL_TOGGLE_DEVTOOLS = "ToggleDevtools";
    exports2.CHANNEL_OPEN_DEVTOOLS_FOR_WINDOW = "OpenDevtoolsForWindow";
    exports2.CHANNEL_GET_ZOOM_LEVEL = "GetZoomLevel";
    exports2.CHANNEL_SET_ZOOM_LEVEL = "SetZoomLevel";
    exports2.CHANNEL_IS_FULL_SCREENABLE = "IsFullScreenable";
    exports2.CHANNEL_IS_FULL_SCREEN = "IsFullScreen";
    exports2.CHANNEL_TOGGLE_FULL_SCREEN = "ToggleFullScreen";
    exports2.CHANNEL_REQUEST_SECONDARY_CLOSE = "RequestSecondaryClose";
    exports2.CHANNEL_REQUEST_CLOSE = "RequestClose";
    exports2.CHANNEL_REQUEST_RELOAD = "RequestReload";
    exports2.CHANNEL_RESTART = "Restart";
    exports2.CHANNEL_APP_STATE_CHANGED = "ApplicationStateChanged";
    exports2.CHANNEL_READ_CLIPBOARD = "ReadClipboard";
    exports2.CHANNEL_WRITE_CLIPBOARD = "WriteClipboard";
    exports2.CHANNEL_KEYBOARD_LAYOUT_CHANGED = "KeyboardLayoutChanged";
    exports2.CHANNEL_IPC_CONNECTION = "IpcConnection";
  }
});

// ../node_modules/@theia/core/lib/electron-browser/preload.js
var require_preload = __commonJS({
  "../node_modules/@theia/core/lib/electron-browser/preload.js"(exports2) {
    "use strict";
    Object.defineProperty(exports2, "__esModule", { value: true });
    exports2.preload = preload;
    var electron_1 = require_electron();
    var disposable_1 = require_disposable();
    var electron_api_1 = require_electron_api();
    var { ipcRenderer, contextBridge } = require("electron");
    var commandHandlers = /* @__PURE__ */ new Map();
    var nextHandlerId = 1;
    var mainMenuId = 1;
    var nextMenuId = mainMenuId + 1;
    var openUrlHandler;
    ipcRenderer.on(electron_api_1.CHANNEL_OPEN_URL, async (event, url, replyChannel) => {
      if (openUrlHandler) {
        event.sender.send(replyChannel, await openUrlHandler(url));
      } else {
        event.sender.send(replyChannel, false);
      }
    });
    function convertMenu(menu, handlerMap) {
      if (!menu) {
        return void 0;
      }
      return menu.map((item) => {
        let handlerId = void 0;
        if (item.execute) {
          handlerId = nextHandlerId++;
          handlerMap.set(handlerId, item.execute);
        }
        return {
          id: item.id,
          submenu: convertMenu(item.submenu, handlerMap),
          accelerator: item.accelerator,
          label: item.label,
          handlerId,
          checked: item.checked,
          enabled: item.enabled,
          role: item.role,
          type: item.type,
          visible: item.visible
        };
      });
    }
    var api = {
      WindowMetadata: { webcontentId: "none" },
      setMenuBarVisible: (visible, windowName) => ipcRenderer.send(electron_api_1.CHANNEL_SET_MENU_BAR_VISIBLE, visible, windowName),
      setMenu: (menu) => {
        commandHandlers.delete(mainMenuId);
        const handlers = /* @__PURE__ */ new Map();
        commandHandlers.set(mainMenuId, handlers);
        ipcRenderer.send(electron_api_1.CHANNEL_SET_MENU, mainMenuId, convertMenu(menu, handlers));
      },
      getSecurityToken: () => ipcRenderer.sendSync(electron_api_1.CHANNEL_GET_SECURITY_TOKEN),
      focusWindow: (name) => ipcRenderer.send(electron_api_1.CHANNEL_FOCUS_WINDOW, name),
      showItemInFolder: (fsPath) => {
        ipcRenderer.send(electron_api_1.CHANNEL_SHOW_ITEM_IN_FOLDER, fsPath);
      },
      getPathForFile: (file) => electron_1.webUtils.getPathForFile(file),
      openWithSystemApp: (location) => {
        ipcRenderer.send(electron_api_1.CHANNEL_OPEN_WITH_SYSTEM_APP, location);
      },
      attachSecurityToken: (endpoint) => ipcRenderer.invoke(electron_api_1.CHANNEL_ATTACH_SECURITY_TOKEN, endpoint),
      popup: async function(menu, x, y, onClosed, windowName) {
        const menuId = nextMenuId++;
        const handlers = /* @__PURE__ */ new Map();
        commandHandlers.set(menuId, handlers);
        const handle = await ipcRenderer.invoke(electron_api_1.CHANNEL_OPEN_POPUP, menuId, convertMenu(menu, handlers), x, y, windowName);
        const closeListener = () => {
          ipcRenderer.removeListener(electron_api_1.CHANNEL_ON_CLOSE_POPUP, closeListener);
          commandHandlers.delete(menuId);
          onClosed();
        };
        ipcRenderer.on(electron_api_1.CHANNEL_ON_CLOSE_POPUP, closeListener);
        return handle;
      },
      closePopup: function(handle) {
        ipcRenderer.send(electron_api_1.CHANNEL_CLOSE_POPUP, handle);
      },
      getTitleBarStyleAtStartup: function() {
        return ipcRenderer.invoke(electron_api_1.CHANNEL_GET_TITLE_STYLE_AT_STARTUP);
      },
      setTitleBarStyle: function(style) {
        ipcRenderer.send(electron_api_1.CHANNEL_SET_TITLE_STYLE, style);
      },
      setBackgroundColor: function(backgroundColor) {
        ipcRenderer.send(electron_api_1.CHANNEL_SET_BACKGROUND_COLOR, backgroundColor);
      },
      setTheme: function(theme) {
        ipcRenderer.send(electron_api_1.CHANNEL_SET_THEME, theme);
      },
      minimize: function() {
        ipcRenderer.send(electron_api_1.CHANNEL_MINIMIZE);
      },
      isMaximized: function() {
        return ipcRenderer.sendSync(electron_api_1.CHANNEL_IS_MAXIMIZED);
      },
      maximize: function() {
        ipcRenderer.send(electron_api_1.CHANNEL_MAXIMIZE);
      },
      unMaximize: function() {
        ipcRenderer.send(electron_api_1.CHANNEL_UNMAXIMIZE);
      },
      close: function() {
        ipcRenderer.send(electron_api_1.CHANNEL_CLOSE);
      },
      onAboutToClose(handler) {
        const h = (event, replyChannel) => {
          handler();
          event.sender.send(replyChannel);
        };
        ipcRenderer.on(electron_api_1.CHANNEL_ABOUT_TO_CLOSE, h);
        return disposable_1.Disposable.create(() => ipcRenderer.off(electron_api_1.CHANNEL_ABOUT_TO_CLOSE, h));
      },
      setOpenUrlHandler(handler) {
        openUrlHandler = handler;
      },
      onWindowEvent: function(event, handler) {
        const h = (_event, evt) => {
          if (event === evt) {
            handler();
          }
        };
        ipcRenderer.on(electron_api_1.CHANNEL_ON_WINDOW_EVENT, h);
        return disposable_1.Disposable.create(() => ipcRenderer.off(electron_api_1.CHANNEL_ON_WINDOW_EVENT, h));
      },
      setCloseRequestHandler: function(handler) {
        ipcRenderer.on(electron_api_1.CHANNEL_REQUEST_CLOSE, async (event, stopReason, confirmChannel, cancelChannel) => {
          try {
            if (await handler(stopReason)) {
              event.sender.send(confirmChannel);
              return;
            }
            ;
          } catch (e) {
            console.warn("exception in close handler ", e);
          }
          event.sender.send(cancelChannel);
        });
      },
      setSecondaryWindowCloseRequestHandler(windowName, handler) {
        const listener = async (event, name, confirmChannel, cancelChannel) => {
          if (name === windowName) {
            try {
              if (await handler()) {
                event.sender.send(confirmChannel);
                ipcRenderer.removeListener(electron_api_1.CHANNEL_REQUEST_SECONDARY_CLOSE, listener);
                return;
              }
              ;
            } catch (e) {
              console.warn("exception in close handler ", e);
            }
            event.sender.send(cancelChannel);
          }
        };
        ipcRenderer.on(electron_api_1.CHANNEL_REQUEST_SECONDARY_CLOSE, listener);
      },
      toggleDevTools: function() {
        ipcRenderer.send(electron_api_1.CHANNEL_TOGGLE_DEVTOOLS);
      },
      openDevToolsForWindow: function(windowName) {
        ipcRenderer.send(electron_api_1.CHANNEL_OPEN_DEVTOOLS_FOR_WINDOW, windowName);
      },
      getZoomLevel: function() {
        return ipcRenderer.invoke(electron_api_1.CHANNEL_GET_ZOOM_LEVEL);
      },
      setZoomLevel: function(desired, windowName) {
        ipcRenderer.send(electron_api_1.CHANNEL_SET_ZOOM_LEVEL, desired, windowName);
      },
      isFullScreenable: function() {
        return ipcRenderer.sendSync(electron_api_1.CHANNEL_IS_FULL_SCREENABLE);
      },
      isFullScreen: function() {
        return ipcRenderer.sendSync(electron_api_1.CHANNEL_IS_FULL_SCREEN);
      },
      toggleFullScreen: function() {
        ipcRenderer.send(electron_api_1.CHANNEL_TOGGLE_FULL_SCREEN);
      },
      requestReload: (newUrl) => ipcRenderer.send(electron_api_1.CHANNEL_REQUEST_RELOAD, newUrl),
      restart: () => ipcRenderer.send(electron_api_1.CHANNEL_RESTART),
      applicationStateChanged: (state) => {
        ipcRenderer.send(electron_api_1.CHANNEL_APP_STATE_CHANGED, state);
      },
      readClipboard() {
        return ipcRenderer.sendSync(electron_api_1.CHANNEL_READ_CLIPBOARD);
      },
      writeClipboard(text) {
        ipcRenderer.send(electron_api_1.CHANNEL_WRITE_CLIPBOARD, text);
      },
      onKeyboardLayoutChanged(handler) {
        return createDisposableListener(electron_api_1.CHANNEL_KEYBOARD_LAYOUT_CHANGED, (event, layout) => {
          handler(layout);
        });
      },
      onData: (handler) => createDisposableListener(electron_api_1.CHANNEL_IPC_CONNECTION, (event, data) => {
        handler(data);
      }),
      sendData: (data) => {
        ipcRenderer.send(electron_api_1.CHANNEL_IPC_CONNECTION, data);
      },
      useNativeElements: !("THEIA_ELECTRON_DISABLE_NATIVE_ELEMENTS" in process.env && process.env.THEIA_ELECTRON_DISABLE_NATIVE_ELEMENTS === "1")
    };
    function createDisposableListener(channel, handler) {
      ipcRenderer.on(channel, handler);
      return disposable_1.Disposable.create(() => ipcRenderer.off(channel, handler));
    }
    function preload() {
      console.log("exposing theia core electron api");
      ipcRenderer.on(electron_api_1.CHANNEL_INVOKE_MENU, (_, menuId, handlerId) => {
        const map = commandHandlers.get(menuId);
        if (map) {
          const handler = map.get(handlerId);
          if (handler) {
            handler();
          }
        }
      });
      api.WindowMetadata.webcontentId = ipcRenderer.sendSync(electron_api_1.CHANNEL_WC_METADATA);
      contextBridge.exposeInMainWorld("electronTheiaCore", api);
    }
  }
});

// ../node_modules/@theia/filesystem/lib/electron-common/electron-api.js
var require_electron_api2 = __commonJS({
  "../node_modules/@theia/filesystem/lib/electron-common/electron-api.js"(exports2) {
    "use strict";
    Object.defineProperty(exports2, "__esModule", { value: true });
    exports2.CHANNEL_SHOW_SAVE = exports2.CHANNEL_SHOW_OPEN = void 0;
    exports2.CHANNEL_SHOW_OPEN = "ShowOpenDialog";
    exports2.CHANNEL_SHOW_SAVE = "ShowSaveDialog";
  }
});

// ../node_modules/@theia/core/electron-shared/electron/index.js
var require_electron2 = __commonJS({
  "../node_modules/@theia/core/electron-shared/electron/index.js"(exports2, module2) {
    module2.exports = require_electron();
  }
});

// ../node_modules/@theia/filesystem/lib/electron-browser/preload.js
var require_preload2 = __commonJS({
  "../node_modules/@theia/filesystem/lib/electron-browser/preload.js"(exports2) {
    "use strict";
    Object.defineProperty(exports2, "__esModule", { value: true });
    exports2.preload = preload;
    var electron_api_1 = require_electron_api2();
    var electron_1 = require_electron2();
    var api = {
      showOpenDialog: (options) => electron_1.ipcRenderer.invoke(electron_api_1.CHANNEL_SHOW_OPEN, options),
      showSaveDialog: (options) => electron_1.ipcRenderer.invoke(electron_api_1.CHANNEL_SHOW_SAVE, options)
    };
    function preload() {
      console.log("exposing theia filesystem electron api");
      electron_1.contextBridge.exposeInMainWorld("electronTheiaFilesystem", api);
    }
  }
});

// src-gen/frontend/preload.js
require_preload().preload();
require_preload2().preload();
//# sourceMappingURL=preload.js.map
