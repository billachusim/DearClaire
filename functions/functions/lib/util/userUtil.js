"use strict";
var __awaiter = (this && this.__awaiter) || function (thisArg, _arguments, P, generator) {
    return new (P || (P = Promise))(function (resolve, reject) {
        function fulfilled(value) { try { step(generator.next(value)); } catch (e) { reject(e); } }
        function rejected(value) { try { step(generator["throw"](value)); } catch (e) { reject(e); } }
        function step(result) { result.done ? resolve(result.value) : new P(function (resolve) { resolve(result.value); }).then(fulfilled, rejected); }
        step((generator = generator.apply(thisArg, _arguments || [])).next());
    });
};
Object.defineProperty(exports, "__esModule", { value: true });
function getUser(db, userId) {
    return __awaiter(this, void 0, void 0, function* () {
        const user = yield db.collection('users').doc(userId).get();
        return user.data();
    });
}
exports.getUser = getUser;
function updateUser(db, user) {
    return __awaiter(this, void 0, void 0, function* () {
        yield db.collection('users').doc(user.userId).set(user);
        return user;
    });
}
exports.updateUser = updateUser;
//# sourceMappingURL=userUtil.js.map