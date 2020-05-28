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
function getSession(db, sessionId) {
    return __awaiter(this, void 0, void 0, function* () {
        const session = yield db.collection('sessions').doc(sessionId).get();
        return session.data();
    });
}
exports.getSession = getSession;
function getSessionFollowers(session) {
    return __awaiter(this, void 0, void 0, function* () {
        return session.followers;
    });
}
exports.getSessionFollowers = getSessionFollowers;
//# sourceMappingURL=sessionUtil.js.map