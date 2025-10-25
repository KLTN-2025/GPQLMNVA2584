let authStoreRef = null;

export function setAuthStore(store) {
  authStoreRef = store;
}

export function getAuthStore() {
  return authStoreRef;
}
